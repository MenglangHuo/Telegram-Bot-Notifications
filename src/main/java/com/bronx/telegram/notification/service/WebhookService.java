package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.model.entity.TelegramBot;

public interface WebhookService {
    void processWebhook(TelegramBot bot, String payload);
}
