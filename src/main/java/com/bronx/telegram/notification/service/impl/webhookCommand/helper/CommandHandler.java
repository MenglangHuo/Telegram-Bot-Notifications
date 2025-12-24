package com.bronx.telegram.notification.service.impl.webhookCommand.helper;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.service.TelegramChannelService;
import com.bronx.telegram.notification.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

    private final RegistrationFlowHandler registrationFlowHandler;
    private final MessageSender messageSender;
    private final TelegramChannelService telegramChannelService;
    private final StatusHandler statusHandler;
    private final CommandMatcher commandMatcher;

    public void handleCommand(Webhook webhook, WebhookMessage message, String text) {
        String chatId = message.getChatId();
        Long botId = webhook.getBot().getId();

        if (text == null) {
            messageSender.sendMessage(botId, chatId,
                    "❌ Empty message received. Please use the menu buttons or type a command.");
            return;
        }

        String normalizedText = commandMatcher.normalizeCommand(text);

        if (commandMatcher.matchesCommand(normalizedText, "/start", "main menu", "start")) {
            sendStartMenu(webhook, message);
        }else if(commandMatcher.matchesCommand(normalizedText, "/finish", "finish")) {
            registrationFlowHandler.finishRegistrationFlow(webhook, message);
        } else if (commandMatcher.matchesCommand(normalizedText, "/register", "register", "📝")) {
            handleRegisterCommand(webhook, message);
        } else if (commandMatcher.matchesCommand(normalizedText, "/status", "status", "📊")) {
            handleStatusCommand(webhook, message);
        } else if (commandMatcher.matchesCommand(normalizedText, "/help", "help", "❓")) {
            handleHelpCommand(webhook, message);
        } else if (commandMatcher.matchesCommand(normalizedText, "/add_new_chat")) {
            handleAddNewChatCommand(webhook);
        } else {
            handleUnknownCommand(webhook, message, text);
        }
    }

    public void sendStartMenu(Webhook webhook, WebhookMessage message) {

        String deepLinkParam = webhook.getDeeplink();
        if (deepLinkParam == null || deepLinkParam.isEmpty()) {
            String welcome = "👋 <b>Welcome to the Employee Portal!</b>\n\n" +
                    "Choose an option below to get started:";

            List<List<String>> keyboard = List.of(
                    List.of("📝 Register", "📊 Status"),
                    List.of("❓ Help")
            );

            messageSender.sendMessageWithKeyboard(webhook.getBot().getId(),
                    message.getChatId(), welcome, keyboard);
        }else{
           String email= EncryptionUtils.decrypt(deepLinkParam);
           log.info("Deeplink parameter detected: {}", email);
           registrationFlowHandler.handleEmployeeCodeInput(webhook, message.getChatId(), email);
        }
    }

    private void handleRegisterCommand(Webhook webhook, WebhookMessage message) {
        registrationFlowHandler.startRegistrationFlow(webhook, message);
    }

    private void handleStatusCommand(Webhook webhook, WebhookMessage message) {
        statusHandler.handleStatus(webhook, message);
    }

    private void handleHelpCommand(Webhook webhook, WebhookMessage message) {
        String helpMessage =
                "ℹ️ <b>Available Commands</b>\n\n" +
                        "━━━━━━━━━━━━━━━━━\n" +
                        "📝 <b>/register</b> - Register with your employee details\n" +
                        "📊 <b>/status</b> - Check your registration status\n" +
                        "❓ <b>/help</b> - Show this help message\n" +
                        "🔙 <b>/start</b> - Return to main menu\n" +
                        "❌ <b>/cancel</b> - Cancel current registration\n\n" +
                        "━━━━━━━━━━━━━━━━━\n" +
                        "💡 <i>Tip: You can also use the buttons below for quick access!</i>";

        messageSender.sendMessage(webhook.getBot().getId(), message.getChatId(), helpMessage);
    }

    private void handleAddNewChatCommand(Webhook webhook) {
        log.info("Handling add new chat with Bot: {}", webhook.getBot().getBotName());
        telegramChannelService.handleAddNewChat(webhook);
    }

    private void handleUnknownCommand(Webhook webhook, WebhookMessage message, String text) {
        log.debug("Unknown input: {}", text);
        messageSender.sendMessage(webhook.getBot().getId(), message.getChatId(),
                "❓ <b>Unknown Command</b>\n\n" +
                        "I don't understand '<code>" + "</code>'\n\n" +
                        "Use /help to see available commands or tap a button from the menu.");
    }

    public boolean isCancelCommand(String text) {
        return text != null && (text.equalsIgnoreCase("/cancel") ||
                text.equalsIgnoreCase("Cancel") ||
                text.contains("Cancel"));
    }
}