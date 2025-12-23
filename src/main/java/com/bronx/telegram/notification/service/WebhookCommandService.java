package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;

public interface WebhookCommandService {
    void handleCommand(Webhook webhook, WebhookMessage message);
}
