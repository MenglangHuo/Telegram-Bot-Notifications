package com.bronx.notification.service;

import com.bronx.notification.dto.notification.NotificationMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;

public interface NotificationService {
    NotificationMessage createAndQueueNotification(TelegramMessageRequest dto);

}
