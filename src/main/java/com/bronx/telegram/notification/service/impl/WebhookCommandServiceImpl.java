package com.bronx.telegram.notification.service.impl;
import com.bronx.telegram.notification.dto.employee.RegistrationResult;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.service.WebhookCommandService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookCommandServiceImpl implements WebhookCommandService {

    private final EmployeeRegistrationService registrationService;
    private final TelegramBotServiceImpl botService;
    private final TelegramChannelServiceImpl telegramChannelService;

    @Override
    public void handleCommand(Webhook webhook, WebhookMessage message) {
        String command = message.getCommand();

        if (command == null) {
            return;
        }

        switch (command) {
            case "/start":
                handleStartCommand(webhook, message);
                break;
            case "/register":
                handleRegisterCommand(webhook, message);
                break;
            case "/help":
                handleHelpCommand(webhook, message);
                break;
            case "/status":
                handleStatusCommand(webhook, message);
                break;
            case "/add_new_chat":
                handleAddNewChat(webhook);
                break;
            default:
                log.debug("Unknown command: {}", command);
        }
    }

    private void handleAddNewChat(Webhook webhook){
       telegramChannelService.handleAddNewChat(webhook);
    }

    private void handleStartCommand(Webhook webhook, WebhookMessage message) {
        String welcomeMessage =
                "<b>Welcome to Notification Bot!</b>\n\n" +
                        "Commands:\n" +
                        "/register - Register with your employee email\n" +
                        "/status - Check your registration status\n" +
                        "/help - Show this help message";
        log.info("start....");

        sendReply(webhook.getBot().getId(), message.getChatId(), welcomeMessage);
    }

    private void handleRegisterCommand(Webhook webhook, WebhookMessage message) {
        String instructionMessage =
                "<b>Employee Registration</b>\n\n" +
                        "Please send your email address in the format:\n" +
                        "/register your.email@company.com";

        // Check if email provided
        JsonNode content = message.getContent();
        if (content.has("message") && content.get("message").has("text")) {
            String text = content.get("message").get("text").asText();
            String[] parts = text.split(" ");

            if (parts.length == 2) {
                String email = parts[1];
                processRegistration(webhook, message, email);
                return;
            }
        }

        sendReply(webhook.getBot().getId(), message.getChatId(), instructionMessage);
    }

    private void processRegistration(Webhook webhook, WebhookMessage message, String email) {
        try {
            RegistrationResult result = registrationService.registerEmployee(
                    email,
                    message.getUserId(),
                    message.getUsername(),
                    message.getChatId()
            );

            String responseMessage;
            if (result.isSuccess()) {
                responseMessage = String.format(
                        "<b>Registration Successful!</b>\n\n" +
                                "Welcome, %s\n" +
                                "You will now receive notifications from your company.",
                        result.getEmployee().getFullName()
                );
            } else {
                responseMessage = String.format(
                        "<b>Registration Failed</b>\n\n" +
                                "%s\n\n" +
                                "Please contact your HR department if you believe this is an error.",
                        result.getMessage()
                );
            }

            sendReply(webhook.getBot().getId(), message.getChatId(), responseMessage);

        } catch (Exception e) {
            log.error("Registration failed", e);
            sendReply(webhook.getBot().getId(), message.getChatId(),
                    "An error occurred during registration. Please try again later.");
        }
    }

    private void handleHelpCommand(Webhook webhook, WebhookMessage message) {
        String helpMessage =
                "<b>Bot Commands</b>\n\n" +
                        "/start - Welcome message\n" +
                        "/register - Register with email\n" +
                        "/status - Check registration\n" +
                        "/help - Show this message";

        sendReply(webhook.getBot().getId(), message.getChatId(), helpMessage);
    }

    private void handleStatusCommand(Webhook webhook, WebhookMessage message) {
        // Implementation for checking status
        sendReply(webhook.getBot().getId(), message.getChatId(),
                "Checking your registration status...");
    }

    private void sendReply(Long botId, String chatId, String text) {
        try {
            TelegramBotClient client = botService.getBotClient(botId);
            if (client != null) {
                client.sendMessageWithReturn(chatId, text,false, "HTML",null);
            }
        } catch (Exception e) {
            log.error("Failed to send reply", e);
        }
    }
}
