package com.bronx.telegram.notification.service.impl;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.repository.SubscriptionRepository;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.service.TelegramBotService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final TelegramBotRepository botRepository;

    // ✅ Use @Value to inject configuration
    @Value("${telegram.webhook.base-url}")
    private String webhookBaseUrl;

    @Value("${telegram.webhook.path}")
    private String webhookPath;

    // ✅ Parse comma-separated list from application.yaml
    @Value("${telegram.webhook.allowed-updates:message,callback_query,my_chat_member,channel_post}")
    private String allowedUpdatesString;

    // Map of bot ID -> bot client (NOT Spring beans)
    private final ConcurrentHashMap<Long, TelegramBotClient> botClients = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class) //every things are ready
    @PostConstruct
    public void initializeBots() {
        log.info("🚀 Initializing Telegram bots...");

        // Validate webhook URL
        if (webhookBaseUrl == null || webhookBaseUrl.isEmpty()) {
            log.error("❌ CRITICAL: telegram.webhook.base-url is not configured!");
            return;
        }

        if (webhookBaseUrl.contains("localhost") || webhookBaseUrl.contains("127.0.0.1")) {
            log.error("❌ CRITICAL: Webhook URL uses localhost!");
            log.error("💡 Telegram CANNOT reach localhost. You must use:");
            log.error("   1. A public domain with SSL (production)");
            log.error("   2. ngrok for local testing: ngrok http 8088");
            log.error("   3. Update application.yaml with the public URL");
            return;
        }

        if (!webhookBaseUrl.startsWith("https://")) {
            log.error("❌ CRITICAL: Webhook URL must use HTTPS!");
            return;
        }

        try {
            List<TelegramBot> activeBots = botRepository.findByStatus(BotStatus.ACTIVE);
            log.info("Found {} active bots to initialize", activeBots.size());

            int successCount = 0;
            for (TelegramBot bot : activeBots) {
                try {
                    registerBot(bot);
                    successCount++;
                } catch (Exception e) {
                    log.error("❌ Failed to initialize bot {}: {}",
                            bot.getBotUsername(), e.getMessage(), e);
                }
            }

            log.info("✅ Bot initialization completed. {}/{} bots active",
                    successCount, activeBots.size());

        } catch (Exception e) {
            log.error("❌ Error during bot initialization", e);
        }
    }

    @Transactional
    public void registerBot(TelegramBot bot) {
        try {
            log.info("📝 Registering bot: {} (name: {})",
                    bot.getBotUsername(),
                    bot.getBotName());

            // Create client instance
            TelegramBotClient client = new TelegramBotClient(bot.getBotToken());

            // Verify token
            JsonNode botInfo = client.verifyToken();
            if (botInfo == null) {
                log.error("❌ Invalid token for bot {}", bot.getBotUsername());
//                bot.setStatus(BotStatus.ERROR);
                bot.setLastErrorMessage("Invalid bot token");
                bot.setLastErrorAt(Instant.now());
                botRepository.save(bot);
                return;
            }

            // Build webhook URL
            String webhookUrl = webhookBaseUrl + webhookPath + "/" + bot.getId();
            bot.setWebhookUrl(webhookUrl);

            // Parse allowed updates
            List<String> allowedUpdates = Arrays.stream(allowedUpdatesString.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // Setup webhook
            log.info("Setting up webhook for bot {}...", bot.getBotUsername());
            boolean webhookSuccess = client.setWebhook(webhookUrl, allowedUpdates);

            if (webhookSuccess) {
                bot.setWebhookVerified(true);
                bot.setStatus(BotStatus.ACTIVE);
//                bot.setErrorCount(0);
                bot.setLastErrorMessage(null);
                log.info("✅ Webhook configured: {}", webhookUrl);

                // Verify webhook info
                JsonNode webhookInfo = client.getWebhookInfo();
                if (webhookInfo != null && webhookInfo.has("url")) {
                    String confirmedUrl = webhookInfo.get("url").asText();
                    if (!confirmedUrl.equals(webhookUrl)) {
                        log.warn("⚠️ Webhook URL mismatch!");
                        log.warn("   Expected: {}", webhookUrl);
                        log.warn("   Got: {}", confirmedUrl);
                    }
                }
            } else {
                bot.setWebhookVerified(false);
//                bot.setStatus(BotStatus.ERROR);
                bot.setLastErrorMessage("Failed to set webhook");
                bot.setLastErrorAt(Instant.now());
                log.error("❌ Failed to set webhook for bot {}", bot.getBotUsername());
            }

            // Save bot
            botRepository.save(bot);

            // Store client only if webhook succeeded
            if (webhookSuccess) {
                botClients.put(bot.getId(), client);
                log.info("✅ Successfully registered bot: {}",
                        bot.getBotUsername());
            }

        } catch (Exception e) {
            log.error("❌ Failed to register bot {}", bot.getId(), e);
//            bot.setStatus(BotStatus.ERROR);
            bot.setLastErrorMessage(e.getMessage());
            bot.setLastErrorAt(Instant.now());
//            bot.setErrorCount(bot.getErrorCount() + 1);
            botRepository.save(bot);
            throw new RuntimeException("Bot registration failed: " + e.getMessage(), e);
        }
    }

    public void unregisterBot(Long botId) {
        TelegramBotClient client = botClients.remove(botId);
        if (client != null) {
            client.shutdown();
            log.info("Unregistered bot: {}", botId);
        }
    }

    @Override
    public TelegramBot findBotForCompany(Long companyId) {
        return null;
    }
//
//    private TelegramBot findBotForCompany(Long companyId) {
//        return botRepository.findFirstByCompanyIdAndStatus(companyId, BotStatus.ACTIVE)
//                .orElseThrow(() -> new RuntimeException("No active bot found for company " + companyId));
//    }

    public TelegramBotClient getBotClient(Long botId) {
        return botClients.get(botId);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down all Telegram bots...");

        botClients.forEach((botId, client) -> {
            try {
                client.shutdown();
                log.info("Shutdown bot: {}", botId);
            } catch (Exception e) {
                log.error("Error shutting down bot {}", botId, e);
            }
        });

        botClients.clear();
        log.info("All bots shutdown completed");
    }

    public boolean isBotRegistered(Long botId) {
        return botClients.containsKey(botId);
    }

    public List<Long> getRegisteredBotIds() {
        return List.copyOf(botClients.keySet());
    }

    public void refreshBot(Long botId) {
        unregisterBot(botId);

        TelegramBot bot = botRepository.findById(botId)
                .orElseThrow(() -> new RuntimeException("Bot not found: " + botId));

        if (bot.getStatus() == BotStatus.ACTIVE) {
            registerBot(bot);
        }
    }
    /**
     * ✅ NEW: Get bot for subscription
     */
    public TelegramBot getBotForSubscription(Long subscriptionId) {
        return botRepository
                .findFirstBySubscriptionIdAndStatus(subscriptionId, BotStatus.ACTIVE)
                .orElse(null);
    }
    private void updateBotError(Long botId, String errorMessage) {
        try {
            botRepository.findById(botId).ifPresent(bot -> {
//                bot.setErrorCount(bot.getErrorCount() + 1);
                bot.setLastErrorMessage(errorMessage);
                bot.setLastErrorAt(Instant.now());

                // Suspend bot if too many errors
//                if (bot.getErrorCount() > 20) {
//                    bot.setStatus(BotStatus.ERROR);
//                    log.error("Bot {} suspended due to too many errors", bot.getBotUsername());
//                }

                botRepository.save(bot);
            });
        } catch (Exception e) {
            log.error("Failed to update bot error stats", e);
        }
    }

    public boolean sendPersonalMessage(
            Long botId,
            String chatId,
            String message,
            boolean urgent) {

        TelegramBotClient client = getBotClient(botId);
        if (client == null) {
            log.error("No client found for bot {}", botId);
            return false;
        }

        try {
            String messageId = client.sendMessageWithReturn(
                    chatId,
                    message,
                    !urgent, // silent = !urgent
                    "HTML",
                    null
            );

            return messageId != null;

        } catch (Exception e) {
            log.error("Failed to send personal message via bot {}: {}",
                    botId, e.getMessage(), e);

            // Update bot error stats
            updateBotError(botId, e.getMessage());
            return false;
        }
    }

    /**
     * ✅ ENHANCED: Send channel message with pin support
     */
    public boolean sendChannelMessage(
            Long botId,
            String chatId,
            String message,
            boolean pinMessage,
            boolean disableNotification) {

        TelegramBotClient client = getBotClient(botId);
        if (client == null) {
            log.error("No client found for bot {}", botId);
            return false;
        }

        try {
            String messageId = client.sendMessageWithReturn(
                    chatId,
                    message,
                    disableNotification,
                    "HTML",
                    null
            );

            if (messageId != null && pinMessage) {
                // Pin the message
                boolean pinned = client.pinMessage(chatId, messageId);
                if (!pinned) {
                    log.warn("Failed to pin message {} in chat {}", messageId, chatId);
                }
            }

            return messageId != null;

        } catch (Exception e) {
            log.error("Failed to send channel message via bot {}: {}",
                    botId, e.getMessage(), e);

            updateBotError(botId, e.getMessage());
            return false;
        }
    }
}

