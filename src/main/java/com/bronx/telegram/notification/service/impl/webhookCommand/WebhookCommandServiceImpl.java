package com.bronx.telegram.notification.service.impl.webhookCommand;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.RegistrationState;
import com.bronx.telegram.notification.service.UserBotStateService;
import com.bronx.telegram.notification.service.WebhookCommandService;
import com.bronx.telegram.notification.service.impl.webhookCommand.helper.CommandHandler;
import com.bronx.telegram.notification.service.impl.webhookCommand.helper.MessageSender;
import com.bronx.telegram.notification.service.impl.webhookCommand.helper.RegistrationFlowHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookCommandServiceImpl implements WebhookCommandService {

    private final RegistrationFlowHandler registrationFlowHandler;
    private final CommandHandler commandHandler;
    private final MessageSender messageSender;
    private final UserBotStateService userBotStateService;

    @Override
    public void handleCommand(Webhook webhook, WebhookMessage message) {
        String text = message.getCommand();
        String chatId = message.getChatId();

        // Normalize input
        if (text != null) {
            text = text.trim();
        }

        // Check for cancel command
        if (commandHandler.isCancelCommand(text)) {
            handleCancelCommand(webhook, message);
            return;
        }

        // Check if user is in registration flow
        RegistrationState currentState = userBotStateService.getUserState(chatId);
        if (currentState != RegistrationState.IDLE) {
            log.info("User in state {}, delegating to state handler", currentState);
            registrationFlowHandler.handleRegistrationFlow(webhook, message, currentState);
            return;
        }

        // Handle regular commands
        commandHandler.handleCommand(webhook, message, text);
    }

    private void handleCancelCommand(Webhook webhook, WebhookMessage message) {
        String chatId = message.getChatId();
        RegistrationState currentState = userBotStateService.getUserState(chatId);

        if (currentState != RegistrationState.IDLE) {
            userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
            messageSender.sendMessage(webhook.getBot().getId(), chatId,
                    "❌ <b>Registration Cancelled</b>\n\nYou can start over anytime with /register");
            commandHandler.sendStartMenu(webhook, message);
        } else {
            messageSender.sendMessage(webhook.getBot().getId(), chatId,
                    "ℹ️ No active process to cancel.");
        }
    }
//
//    @Override
//    public void handleCommand(Webhook webhook, WebhookMessage message) {
//        String text = message.getCommand(); // Assume this gets the message text
//        String chatId = message.getChatId();// 1. Check if the user is in the middle of a flow (State Management)
//
//        // Normalize input: trim whitespace
//        if (text != null) {
//            text = text.trim();
//        }
//
//        // Check for cancel during registration
//        if (text != null && (text.equalsIgnoreCase("/cancel") ||
//                text.equalsIgnoreCase("Cancel") ||
//                text.contains("Cancel"))) {
//            handleCancelCommand(webhook, message);
//            return;
//        }
//
//
//        RegistrationState currentState = userBotStateService.getUserState(chatId);
//        if(currentState!= RegistrationState.IDLE){
//            log.info("User in state {}, delegating to state handler", currentState);
//            handleRegistrationFlow(webhook, message, currentState);
//            return;
//        }
//
//        if (text == null) {
//            sendReply(webhook.getBot().getId(), chatId,
//                    "❌ Empty message received. Please use the menu buttons or type a command.");
//            return;
//        }
//        // Normalize command for matching
//        String normalizedText = normalizeCommand(text);
//
//        // Match commands (case-insensitive and emoji-tolerant)
//        if (matchesCommand(normalizedText, "/start", "main menu", "start")) {
//            handleStartCommand(webhook, message);
//        } else if (matchesCommand(normalizedText, "/register", "register", "📝")) {
//            startRegistrationFlow(webhook, message);
//        } else if (matchesCommand(normalizedText, "/status", "status", "📊")) {
//            handleStatusCommand(webhook, message);
//        } else if (matchesCommand(normalizedText, "/help", "help", "❓")) {
//            handleHelpCommand(webhook, message);
//        } else if (matchesCommand(normalizedText, "/add_new_chat")) {
//            log.info("Handling add new chat with Bot: {}", webhook.getBot().getBotName());
//            handleAddNewChat(webhook);
//        } else {
//            handleUnknownCommand(webhook, message, text);
//        }
//    }
//
//
//
//    private void handleAddNewChat(Webhook webhook){
//       telegramChannelService.handleAddNewChat(webhook);
//    }
//
//    private void handleHelpCommand(Webhook webhook, WebhookMessage message) {
//        String helpMessage =
//                "ℹ️ <b>Available Commands</b>\n\n" +
//                        "━━━━━━━━━━━━━━━━━\n" +
//                        "📝 <b>/register</b> - Register with your employee details\n" +
//                        "📊 <b>/status</b> - Check your registration status\n" +
//                        "❓ <b>/help</b> - Show this help message\n" +
//                        "🔙 <b>/start</b> - Return to main menu\n" +
//                        "❌ <b>/cancel</b> - Cancel current registration\n\n" +
//                        "━━━━━━━━━━━━━━━━━\n" +
//                        "💡 <i>Tip: You can also use the buttons below for quick access!</i>";
//
//        sendReply(webhook.getBot().getId(), message.getChatId(), helpMessage);
//    }
//
//    private void sendReply(Long botId, String chatId, String text) {
//        try {
//            TelegramBotClient client = botService.getBotClient(botId);
//            if (client != null) {
//                client.sendMessageWithReturn(chatId, text,false, "HTML",null);
//            }
//        } catch (Exception e) {
//            log.error("Failed to send reply", e);
//        }
//    }
//
//    // Enhanced start registration with confirmation
//    private void startRegistrationFlow(Webhook webhook, WebhookMessage message) {
//        String chatId = message.getChatId();
//
//        // Check if already registered
//        boolean isEmpExisting = employeeRepository.existsByTelegramChatId(chatId);
//        if (isEmpExisting) {
//            sendReply(webhook.getBot().getId(), chatId,
//                    "ℹ️ <b>Already Registered</b>\n\n" +
//                            "You are already registered in the system.\n\n" +
//                            "Use /status to view your information.");
//            return;
//        }
//
//        userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_EMAIL);
//        sendReply(webhook.getBot().getId(), chatId,
//                "📝 <b>Registration Process</b>\n\n" +
//                        "Step 1 of 3\n\n" +
//                        "Please enter your <b>Email Address</b>:\n\n" +
//                        "<i>Use /cancel to stop registration</i>");
//    }
//
//
//    private void sendReplyWithKeyboard(Long botId, String chatId, String text, List<List<String>> buttons) {
//        try {
//            TelegramBotClient client = botService.getBotClient(botId);
//            ObjectNode keyboardNode =objectMapper.createObjectNode();
//            ArrayNode keyboardArray = keyboardNode.putArray("keyboard");
//
//            for (List<String> row : buttons) {
//                ArrayNode rowNode = keyboardArray.addArray();
//                row.forEach(rowNode::add);
//            }
//            keyboardNode.put("resize_keyboard", true);
//            keyboardNode.put("one_time_keyboard", true);
//            // Send via your client (ensure your client supports passing the 'reply_markup' parameter)
//            client.sendMessageWithKeyboard(chatId, text, "HTML", keyboardNode.toString());
//        } catch (Exception e) {
//            log.error("Failed to send reply with keyboard", e);
//        }
//    }
//
//    private void handleRegistrationFlow(Webhook webhook, WebhookMessage message, RegistrationState currentState) {
//        String userInput = message.getCommand();
//        String chatId = message.getChatId();
//        Long botId = webhook.getBot().getId();
//
//        switch (currentState) {
//            case AWAITING_EMAIL:
//                // Validate email format
//                if (!isValidEmail(userInput)) {
//                    sendReply(botId, chatId, "❌ Invalid email format. Please enter a valid email address:");
//                    return; // Stay in current state
//                }
//
//                userBotStateService.saveTemporaryEmail(chatId, userInput);
//                userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_EMP_CODE);
//                sendReply(botId, chatId, "✅ Email saved.\n\nStep 2: Please enter your <b>Employee Code</b>:");
//                break;
//
//            case AWAITING_EMP_CODE:
//                // Validate employee code (add your validation rules)
//                if (userInput == null || userInput.trim().isEmpty()) {
//                    sendReply(botId, chatId, "❌ Employee code cannot be empty. Please try again:");
//                    return;
//                }
//
//                userBotStateService.saveTemporaryEmployeeCode(chatId, userInput.trim());
//                userBotStateService.updateUserState(chatId, RegistrationState.AWAITING_CONTACT);
//                sendReply(botId, chatId, "✅ Employee code saved.\n\nFinal Step: Please provide your <b>Contact Number</b>:");
//                break;
//
//            case AWAITING_CONTACT:
//                // Validate contact number
//                if (!isValidContact(userInput)) {
//                    sendReply(botId, chatId, "❌ Invalid contact number. Please enter a valid phone number:");
//                    return;
//                }
//
//                try {
//                    String result = userBotStateService.completeRegistration(webhook, message, userInput.trim());
//
//                    if ("Success".equals(result)) {
//                        sendReply(botId, chatId,
//                                "🎉 <b>Congratulations!</b>\n\n" +
//                                        "You have successfully completed your registration.\n\n" +
//                                        "You can now access all features.");
//
//                        // Send main menu after successful registration
//                        sendMenu(chatId, botId);
//                    } else {
//                        sendReply(botId, chatId,
//                                "❌ <b>Registration Failed</b>\n\n" + result + "\n\nPlease try again.");
//                        userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
//                    }
//                } catch (Exception e) {
//                    log.error("Registration failed for chatId {}: {}", chatId, e.getMessage(), e);
//                    sendReply(botId, chatId,
//                            "❌ <b>Registration Error</b>\n\n" +
//                                    "An error occurred during registration. Please try again later or contact support.");
//                    userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
//                }
//                break;
//
//            default:
//                log.warn("Unhandled registration state: {} for chatId: {}", currentState, chatId);
//                sendReply(botId, chatId,
//                        "❌ <b>Invalid State</b>\n\nSomething went wrong. Please start over with /register");
//                userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
//        }
//    }
//    // Helper validation methods
//    private boolean isValidEmail(String email) {
//        if (email == null || email.trim().isEmpty()) {
//            return false;
//        }
//        // Basic email validation regex
//        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
//    }
//
//    private String normalizeCommand(String text) {
//        if (text == null) return "";
//        return text.trim().toLowerCase();
//    }
//    private boolean matchesCommand(String normalizedText, String... patterns) {
//        for (String pattern : patterns) {
//            if (normalizedText.equals(pattern.toLowerCase()) ||
//                    normalizedText.contains(pattern.toLowerCase())) {
//                return true;
//            }
//        }
//        return false;
//    }
//    private void handleCancelCommand(Webhook webhook, WebhookMessage message) {
//        String chatId = message.getChatId();
//        RegistrationState currentState = userBotStateService.getUserState(chatId);
//
//        if (currentState != RegistrationState.IDLE) {
//            userBotStateService.updateUserState(chatId, RegistrationState.IDLE);
//            sendReply(webhook.getBot().getId(), chatId,
//                    "❌ <b>Registration Cancelled</b>\n\nYou can start over anytime with /register");
//            handleStartCommand(webhook, message);
//        } else {
//            sendReply(webhook.getBot().getId(), chatId,
//                    "ℹ️ No active process to cancel.");
//        }
//    }
//    private void handleStatusCommand(Webhook webhook, WebhookMessage message) {
//        String chatId = message.getChatId();
//        Long botId = webhook.getBot().getId();
//
//        try {
//            log.info("Fetching status for chatId: {}", chatId);
//
//            // Check if user is in registration process
//            RegistrationState currentState = userBotStateService.getUserState(chatId);
//            if (currentState != RegistrationState.IDLE) {
//                String stateMessage = getRegistrationStateMessage(currentState);
//                sendReply(botId, chatId,
//                        "⏳ <b>Registration In Progress</b>\n\n" +
//                                "Current step: " + stateMessage + "\n\n" +
//                                "Complete your registration or use /cancel to start over.");
//                return;
//            }
//
//            // Try to find employee by chat ID
//            Optional<Employee> employeeOpt = employeeRepository.findByTelegramChatId(chatId);
//
//            if (employeeOpt.isEmpty()) {
//                sendReply(botId, chatId,
//                        "❌ <b>Not Registered</b>\n\n" +
//                                "You are not registered in the system yet.\n\n" +
//                                "📝 Use /register to create your account.");
//                return;
//            }
//
//            Employee employee = employeeOpt.get();
//
//            // Build status message with full employee information
//            StringBuilder statusMsg = new StringBuilder();
//            statusMsg.append("✅ <b>Registration Status: Active</b>\n\n");
//            statusMsg.append("👤 <b>Personal Information</b>\n");
//            statusMsg.append("━━━━━━━━━━━━━━━━━\n");
//
//            if (employee.getFullName() != null && !employee.getFullName().isEmpty()) {
//                statusMsg.append("📛 Name: ").append(escapeHtml(employee.getFullName())).append("\n");
//            }
//
//            if (employee.getEmployeeCode() != null && !employee.getEmployeeCode().isEmpty()) {
//                statusMsg.append("🆔 Employee Code: ").append(escapeHtml(employee.getEmployeeCode())).append("\n");
//            }
//
//            if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
//                statusMsg.append("📧 Email: ").append(escapeHtml(employee.getEmail())).append("\n");
//            }
//
//            if (employee.getContact() != null && !employee.getContact().isEmpty()) {
//                statusMsg.append("📱 Contact: ").append(escapeHtml(employee.getContact())).append("\n");
//            }
//
//            statusMsg.append("\n💼 <b>Company Information</b>\n");
//            statusMsg.append("━━━━━━━━━━━━━━━━━\n");
//
//            if (employee.getCompany() != null) {
//                Company company = employee.getCompany();
//                if (company.getName() != null) {
//                    statusMsg.append("🏢 Company: ").append(escapeHtml(company.getName())).append("\n");
//                }
//                if (company.getCode() != null) {
//                    statusMsg.append("🔑 Company Code: ").append(escapeHtml(company.getCode())).append("\n");
//                }
//            }
//
//
//            if (employee.getRole() != null && !employee.getRole().isEmpty()) {
//                statusMsg.append("💼 Position: ").append(escapeHtml(employee.getRole())).append("\n");
//            }
//
//            statusMsg.append("\n📊 <b>Account Details</b>\n");
//            statusMsg.append("━━━━━━━━━━━━━━━━━\n");
//            statusMsg.append("🆔 Chat ID: <code>").append(chatId).append("</code>\n");
//
//
//            if (employee.getCreatedAt() != null) {
//                statusMsg.append("📅 Registered: ")
//                        .append(formatDate(employee.getCreatedAt())).append("\n");
//            }
//
//            if (employee.getUpdatedAt() != null) {
//                statusMsg.append("🔄 Last Updated: ")
//                        .append(formatDate(employee.getUpdatedAt())).append("\n");
//            }
//
//            statusMsg.append("\n💡 <i>Need to update your info? Contact your administrator.</i>");
//
//            sendReply(botId, chatId, statusMsg.toString());
//
//            log.info("Status sent successfully for employee: {}", employee.getEmployeeCode());
//
//        } catch (Exception e) {
//            log.error("Error fetching status for chatId: {}", chatId, e);
//            sendReply(botId, chatId,
//                    "❌ <b>Error</b>\n\n" +
//                            "Unable to retrieve your status. Please try again later or contact support.");
//        }
//    }
//    private String getRegistrationStateMessage(RegistrationState state) {
//        switch (state) {
//            case AWAITING_EMAIL:
//                return "Waiting for Email Address";
//            case AWAITING_EMP_CODE:
//                return "Waiting for Employee Code";
//            case AWAITING_CONTACT:
//                return "Waiting for Contact Number";
//            default:
//                return "Unknown";
//        }
//    }
//
//    private String getStatusEmoji(BotStatus status) {
//        if (status == null) return "⚪";
//        switch (status) {
//            case ACTIVE:
//                return "🟢";
//            case INACTIVE:
//                return "🔴";
//            case PENDING:
//                return "🟡";
//            case SUSPENDED:
//                return "🟠";
//            case TERMINATE:
//                return "⚫";
//
//            default:
//                return "⚪";
//        }
//    }
//    private String formatDate(Instant instant) {
//        if (instant == null) return "N/A";
//        try {
//            return DateTimeFormatter
//                    .ofPattern("MMM dd, yyyy HH:mm")
//                    .withZone(ZoneId.systemDefault())
//                    .format(instant);
//        } catch (Exception e) {
//            return instant.toString();
//        }
//    }
//    private String escapeHtml(String text) {
//        if (text == null) return "";
//        return text
//                .replace("&", "&amp;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;")
//                .replace("\"", "&quot;");
//    }
//
//    private void handleUnknownCommand(Webhook webhook, WebhookMessage message, String text) {
//        log.debug("Unknown input: {}", text);
//        sendReply(webhook.getBot().getId(), message.getChatId(),
//                "❓ <b>Unknown Command</b>\n\n" +
//                        "I don't understand '<code>" + escapeHtml(text) + "</code>'\n\n" +
//                        "Use /help to see available commands or tap a button from the menu.");
//    }
//
//    private void handleStartCommand(Webhook webhook, WebhookMessage message) {
//        String welcome = "👋 <b>Welcome to the Employee Portal!</b>\n\n" +
//                "Choose an option below to get started:";
//
//        List<List<String>> keyboard = List.of(
//                List.of("📝 Register", "📊 Status"),
//                List.of("❓ Help")
//        );
//
//        sendReplyWithKeyboard(webhook.getBot().getId(), message.getChatId(), welcome, keyboard);
//    }
//    private boolean isValidContact(String contact) {
//        if (contact == null || contact.trim().isEmpty()) {
//            return false;
//        }
//        // Validate phone number (adjust pattern for your needs)
//        // This accepts formats like: +1234567890, 1234567890, (123) 456-7890
//        String cleaned = contact.replaceAll("[^0-9+]", "");
//        return cleaned.length() >= 8 && cleaned.length() <= 15;
//    }
//
//    private void sendMenu(String chatId,Long botId) {
//
//        TelegramBotClient client = botService.getBotClient(botId);
//        ObjectNode replyMarkup = objectMapper.createObjectNode();
//        ArrayNode keyboard = replyMarkup.putArray("keyboard");
//
//        // Row 1
//        ArrayNode row1 = keyboard.addArray();
//        row1.add("📝 Register");
//
//        // Row 2
//        ArrayNode row2 = keyboard.addArray();
//        row2.add("📊 Status");
//        row2.add("❓ Help");
//
//        replyMarkup.put("resize_keyboard", true);
//        replyMarkup.put("one_time_keyboard", false);
//
//        // 2. Call the new client method
//        client.sendMessageWithKeyboard(
//                chatId,
//                "Please select an option from the menu:",
//                "HTML",
//                replyMarkup.toString()
//        );
//    }
}

