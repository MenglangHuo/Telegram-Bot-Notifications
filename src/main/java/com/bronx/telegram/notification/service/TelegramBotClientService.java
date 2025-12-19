package com.bronx.telegram.notification.service;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface TelegramBotClientService {
    JsonNode verifyToken();
    boolean setWebhook(String webhookUrl, List<String> allowedUpdates);
    JsonNode getWebhookInfo();
    boolean sendMessage(String chatId, String text);
    boolean sendMessage(String chatId, String text, Boolean silent);
    String sendMessageWithReturn(String chatId, String text, Boolean silent,
                                 String parseMode, Integer replyToMessageId);
    void shutdown();
}
