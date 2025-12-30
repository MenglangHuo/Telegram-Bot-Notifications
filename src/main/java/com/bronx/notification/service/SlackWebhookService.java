package com.bronx.notification.service;

import com.bronx.notification.model.entity.Notification;


public interface SlackWebhookService {
    void sendNotificationToSlack(Notification notification);
    void sendNotificationToSlackSync(Notification notification);
    void sendMessage(String message);

}
