package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.notification.NotificationMessage;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
import com.bronx.telegram.notification.model.entity.Webhook;

public interface NotificationQueueService {
    void queuePersonalNotification(NotificationPersonal notification);
    void queueChannelNotification(NotificationChannel notification);
    void queueWebhookProcessing(Webhook webhook);
    void requeueForRetry(NotificationMessage message,long delaySeconds);

}
