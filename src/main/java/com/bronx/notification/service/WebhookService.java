package com.bronx.notification.service;

import com.bronx.notification.model.entity.TelegramBot;

public interface WebhookService {
    void processWebhook(TelegramBot bot, String payload);
}
