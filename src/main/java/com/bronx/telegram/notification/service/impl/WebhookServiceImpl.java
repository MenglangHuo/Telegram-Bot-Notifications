package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.repository.TelegramChannelRepository;
import com.bronx.telegram.notification.repository.WebhookRepository;
import com.bronx.telegram.notification.service.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WebhookServiceImpl implements WebhookService {

    private final WebhookRepository webhookRepository;
    private final NotificationQueueServiceImpl queueService;
    private final ObjectMapper objectMapper;
    private final TelegramChannelRepository telegramChannelRepository;

    @Override
    public void processWebhook(TelegramBot bot, String payload) {
        try {
            JsonNode update = objectMapper.readTree(payload);
            Long updateId = update.get("update_id").asLong();

            // Check if already processed
            if (webhookRepository.existsByUpdateId(updateId)) {
                log.debug("Webhook update {} already processed", updateId);
                return;
            }

            Webhook webhook = new Webhook();
            webhook.setBot(bot);
            webhook.setUpdateId(updateId);
            webhook.setContent(update);
            webhook.setProcessed(false);

            // Extract different update types
            if (update.has("message")) {
                extractMessageDetails(webhook, update.get("message"));
            } else if (update.has("callback_query")) {
                extractCallbackDetails(webhook, update.get("callback_query"));
            } else if (update.has("my_chat_member")) {
                // ✅ Handle bot added to channel/group
                extractChatMemberDetails(webhook, update.get("my_chat_member"));
            } else if (update.has("channel_post")) {
                extractChannelPostDetails(webhook, update.get("channel_post"));
            }

            webhook = webhookRepository.save(webhook);

            // Queue for background processing
            queueService.queueWebhookProcessing(webhook);

            log.info("✅ Queued webhook {} for processing", webhook.getId());

        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    private void extractMessageDetails(Webhook webhook, JsonNode message) {
        if (message.has("chat")) {
            webhook.setChatId(message.get("chat").get("id").asText());
        }

        if (message.has("from")) {
            JsonNode from = message.get("from");
            webhook.setUserId(from.get("id").asText());
            if (from.has("username")) {
                webhook.setUsername(from.get("username").asText());
            }
        }

        if (message.has("text")) {
            String text = message.get("text").asText();
            webhook.setMessageType("text");

            if (text.startsWith("/")) {
                webhook.setCommand(text.split(" ")[0]);
            }
        }
    }

    private void extractCallbackDetails(Webhook webhook, JsonNode callbackQuery) {
        webhook.setMessageType("callback_query");

        if (callbackQuery.has("from")) {
            JsonNode from = callbackQuery.get("from");
            webhook.setUserId(from.get("id").asText());
            if (from.has("username")) {
                webhook.setUsername(from.get("username").asText());
            }
        }

        if (callbackQuery.has("message") && callbackQuery.get("message").has("chat")) {
            webhook.setChatId(callbackQuery.get("message").get("chat").get("id").asText());
        }
    }

    /**
     * ✅ Extract my_chat_member details (bot added/removed from channel)
     */
    private void extractChatMemberDetails(Webhook webhook, JsonNode chatMember) {
        webhook.setMessageType("my_chat_member");

        if (chatMember.has("chat")) {
            JsonNode chat = chatMember.get("chat");
            webhook.setChatId(chat.get("id").asText());

            var chartId=webhook.getChatId();
            var chatType=chat.has("type") ? chat.get("type").asText() : "unknown";
            var chartTitle=chat.has("title") ? chat.get("title").asText() : "N/A";
            var chartDescription=chat.has("description") ? chat.get("description").asText() : "N/A";


            log.info("=== Chat Member Update ===");
            log.info("Chat ID: {}", chartId);
            log.info("Chat Type: {}",chatType);
            log.info("Chat Title: {}", chartTitle);
            log.info("Description chart: {}",chartDescription);
            log.info("All message: {}",chat.toString());
            log.info("chart member: {}",chatMember.toString());

        }

        if (chatMember.has("from")) {
            JsonNode from = chatMember.get("from");
            webhook.setUserId(from.get("id").asText());
            if (from.has("username")) {
                webhook.setUsername(from.get("username").asText());
            }
            log.info("chatMember from {} → {}", from.has("username"), from.toString());
        }

        // Log status change
        if (chatMember.has("old_chat_member") && chatMember.has("new_chat_member")) {
            String oldStatus = chatMember.get("old_chat_member").get("status").asText();
            String newStatus = chatMember.get("new_chat_member").get("status").asText();
            log.info("Status changed: {} → {}", oldStatus, newStatus);

            // Set a flag for easy identification
            webhook.setCommand("/add_new_chat");
        }

    }

    private void extractChannelPostDetails(Webhook webhook, JsonNode channelPost) {
        webhook.setMessageType("channel_post");

        if (channelPost.has("chat")) {
            webhook.setChatId(channelPost.get("chat").get("id").asText());
        }
    }
}
