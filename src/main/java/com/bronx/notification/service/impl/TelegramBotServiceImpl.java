package com.bronx.notification.service.impl;
import com.bronx.notification.configs.TelegramBotClientFactory;
import com.bronx.notification.model.entity.TelegramBot;
import com.bronx.notification.model.enumz.BotStatus;
import com.bronx.notification.repository.TelegramBotRepository;
import com.bronx.notification.service.TelegramBotService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramBotServiceImpl implements TelegramBotService {

    private final TelegramBotRepository botRepository;
    private final TelegramBotClientFactory botClientFactory;

    // ✅ Use @Value to inject configuration
    @Value("${telegram.webhook.base-url}")
    private String webhookBaseUrl;

    @Value("${telegram.webhook.path}")
    private String webhookPath;

    // ✅ Parse comma-separated list from application.yaml
    @Value("${telegram.webhook.allowed-updates:message,callback_query,my_chat_member,channel_post}")
    private String allowedUpdatesString;

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
            TelegramBotClient client = botClientFactory.getClient(String.valueOf(bot.getId()));

            // Verify token
            JsonNode botInfo = client.verifyToken();
            if (botInfo == null) {
                log.error("❌ Invalid token for bot {}", bot.getBotUsername());
                botRepository.save(bot);
                return;
            }

            // Build webhook URL
            String webhookUrl = webhookBaseUrl + webhookPath + "/" + bot.getId();
            bot.setWebhookUrl(webhookUrl);

            // Parse allowed updates
            List<String> allowedUpdates = Arrays.stream(allowedUpdatesString.split(","))
                    .map(String::trim).toList();

            // Setup webhook
            log.info("Setting up webhook for bot {}...", bot.getBotUsername());
            boolean webhookSuccess = client.setWebhook(webhookUrl, allowedUpdates);

            if (webhookSuccess) {
                bot.setStatus(BotStatus.ACTIVE);
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
                log.error("❌ Failed to set webhook for bot {}", bot.getBotUsername());
            }

            // Save bot
            botRepository.save(bot);

        } catch (Exception e) {
            log.error("❌ Failed to register bot {}", bot.getId(), e);
            botRepository.save(bot);
            throw new RuntimeException("Bot registration failed: " + e.getMessage(), e);
        }
    }

    public void unregisterBot(Long botId) {
        botClientFactory.invalidateClient(botId);
    }

    @Override
    public TelegramBot findBotForCompany(Long companyId) {
        return null;
    }
    public TelegramBotClient getBotClient(Long botId) {
        return botClientFactory.getClient(String.valueOf(botId));
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down all Telegram bots...");
        botClientFactory.shutdown();

//        botClients.forEach((botId, client) -> {
//            try {
//                client.shutdown();
//                log.info("Shutdown bot: {}", botId);
//            } catch (Exception e) {
//                log.error("Error shutting down bot {}", botId, e);
//            }
//        });
//
//        botClients.clear();
        log.info("All bots shutdown completed");
    }

    public void refreshBot(Long botId) {
        unregisterBot(botId);

        TelegramBot bot = botRepository.findById(botId)
                .orElseThrow(() -> new RuntimeException("Bot not found: " + botId));

        if (bot.getStatus() == BotStatus.ACTIVE) {
            registerBot(bot);
        }
    }


}

