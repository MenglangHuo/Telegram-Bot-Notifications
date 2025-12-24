package com.bronx.telegram.notification.service.impl.webhookCommand.helper;

import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.RegistrationState;
import com.bronx.telegram.notification.repository.EmployeeRepository;
import com.bronx.telegram.notification.service.UserBotStateService;
import com.bronx.telegram.notification.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationFlowHandler {

    private final UserBotStateService userBotStateService;
    private final EmployeeRepository employeeRepository;
    private final MessageSender messageSender;

    public void startRegistrationFlow(Webhook webhook, WebhookMessage message) {
        String chatId = message.getChatId();
        Long botId = webhook.getBot().getId();

        // Check if already registered
        boolean isEmpExisting = employeeRepository.existsByTelegramChatId(chatId);
        if (isEmpExisting) {
            messageSender.sendMessage(botId, chatId,
                    "ℹ️ <b>Already Registered</b>\n\n" +
                            "You are already registered in the system.\n\n" +
                            "Use /status to view your information.");
            return;
        }

        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_EMAIL);
        messageSender.sendMessage(botId, chatId,
                "📝 <b>Registration Process</b>\n\n" +
                        "Step 1 of 5\n\n" +
                        "Please enter your <b>Email Address</b>:\n\n" +
                        "<i>Use /cancel to stop registration</i>");
    }

    public void handleRegistrationFlow(Webhook webhook, WebhookMessage message,
                                       RegistrationState currentState) {
        String userInput = message.getCommand();
        String chatId = message.getChatId();

        switch (currentState) {
            case AWAITING_EMP_CODE:
                handleEmployeeCodeInput(webhook, chatId, userInput);
                break;
            case AWAITING_EMAIL:
                handleEmailInput(webhook, chatId, userInput);
                break;
            case AWAITING_CONTACT:
                handleContactInput(webhook, chatId, userInput);
                break;
            case AWAITING_FULL_NAME:
                handleFullNameInput(webhook, chatId, userInput);
                break;
            case AWAITING_ROLE:
                handleRoleInput(webhook,message, chatId, userInput);
                break;
            default:
                handleInvalidState(webhook, chatId);
        }
    }

    public void finishRegistrationFlow(Webhook webhook, WebhookMessage message) {
        String chatId = message.getChatId();
        Long botId = webhook.getBot().getId();

        RegistrationState currentState = userBotStateService.getUserState(chatId);
        if (currentState != RegistrationState.AWAITING_ROLE &&
            currentState != RegistrationState.AWAITING_FULL_NAME &&
            currentState != RegistrationState.AWAITING_CONTACT) {
            messageSender.sendMessage(botId, chatId,
                    "❌ <b>Cannot Finish Registration</b>\n\n" +
                            "You are not at the final step of registration.\n\n" +
                            "Please complete all steps before finishing.");
            return;
        }
        completeRegistration(webhook, message, chatId);
    }

    public void handleEmailInput(Webhook webhook, String chatId, String userInput) {
        Long botId = webhook.getBot().getId();

        if (!ValidationUtils.isValidEmail(userInput)) {
            messageSender.sendMessage(botId, chatId,
                    "❌ Invalid email format. Please enter a valid email address:");
            return;
        }

        userBotStateService.saveTemporaryEmail(chatId, userInput);
        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_EMP_CODE);
        messageSender.sendMessage(botId, chatId,
                "✅ Email saved.\n\nStep 3: Please enter your <b>Employee Contact Number</b>:");
    }

    private void handleFullNameInput(Webhook webhook, String chatId, String fullName) {
        Long botId = webhook.getBot().getId();

        userBotStateService.saveFullName(chatId, fullName);
        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_ROLE);
        messageSender.sendMessage(botId, chatId,
                "✅ FullName saved.\n\nStep 4: Please enter your <b>Employee Role</b>\n\n Or Finish Registration: /finish");
    }

    public void handleRoleInput(Webhook webhook,WebhookMessage message, String chatId, String role) {
        userBotStateService.saveRole(chatId, role);
        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_FINISH);
        completeRegistration(webhook, message, chatId);
    }

    public void handleEmployeeCodeInput(Webhook webhook, String chatId, String userInput) {
        Long botId = webhook.getBot().getId();

        if (userInput == null || userInput.trim().isEmpty()) {
            messageSender.sendMessage(botId, chatId,
                    "❌ Employee code cannot be empty. Please try again:");
            return;
        }

        userBotStateService.saveTemporaryEmployeeCode(chatId, userInput.trim());
        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_CONTACT);
        messageSender.sendMessage(botId, chatId,
                "✅ Employee code saved.\n\n Step 2: Please provide your <b>Employee Email</b>:");
    }

    public void handleContactInput(Webhook webhook,
                                    String chatId, String userInput) {
        Long botId = webhook.getBot().getId();

        userBotStateService.saveContact(chatId, userInput.trim());
        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_FULL_NAME);
        messageSender.sendMessage(botId, chatId,
                "✅ Contact saved.\n\nStep 4: Please enter your <b>Employee FullName</b>\n\n Or Finish Registration: /finish");

    }

    public void completeRegistration(Webhook webhook, WebhookMessage message,
                                      String chatId) {
        Long botId = webhook.getBot().getId();

        try {
            String result = userBotStateService.completeRegistration(webhook, message);

            if ("Success".equals(result)) {
                messageSender.sendMessage(botId, chatId,
                        "🎉 <b>Congratulations!</b>\n\n" +
                                "You have successfully completed your registration.\n\n" +
                                "You can now access all features.");

                // Send main menu after successful registration
                sendMainMenu(chatId, botId);
            } else {
                messageSender.sendMessage(botId, chatId,
                        "❌ <b>Registration Failed</b>\n\n" + result + "\n\nPlease try again.");
                userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
            }
        } catch (Exception e) {
            log.error("Registration failed for chatId {}: {}", chatId, e.getMessage(), e);
            messageSender.sendMessage(botId, chatId,
                    "❌ <b>Registration Error</b>\n\n" +
                            "An error occurred during registration. Please try again later or contact support.");
            userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
        }
    }

    private void handleInvalidState(Webhook webhook, String chatId) {
        Long botId = webhook.getBot().getId();
        log.warn("Unhandled registration state for chatId: {}", chatId);
        messageSender.sendMessage(botId, chatId,
                "❌ <b>Invalid State</b>\n\nSomething went wrong. Please start over with /register");
        userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
    }

    private void sendMainMenu(String chatId, Long botId) {
        List<List<String>> keyboard = List.of(
                List.of("📝 Register"),
                List.of( "📊 Status"),
                List.of("❓ Help")
        );
        messageSender.sendMessageWithKeyboard(botId, chatId,
                "Please Check Your Information:", keyboard);
    }
}
