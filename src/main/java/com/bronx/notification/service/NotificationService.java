package com.bronx.notification.service;

import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;

public interface NotificationService {
    void createAndQueueNotification(TelegramMessageRequest dto);

}
