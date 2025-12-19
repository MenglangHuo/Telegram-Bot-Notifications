package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;

public interface WebhookCommandService {
    void handleCommand(Webhook webhook, WebhookMessage message);
//    void handleAddNewChat(Webhook webhook);
//    void handleStartCommand(Webhook webhook, WebhookMessage message);
//    void handleRegisterCommand(Webhook webhook, WebhookMessage message);
//    void processRegistration(Webhook webhook, WebhookMessage message, String email);
//    void handleHelpCommand(Webhook webhook, WebhookMessage message);
//    void handleStatusCommand(Webhook webhook, WebhookMessage message);
//    void sendReply(Long botId, String chatId, String text);

}
