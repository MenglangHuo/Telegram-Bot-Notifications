package com.bronx.telegram.notification.service.impl;
import com.bronx.telegram.notification.configs.RabbitMqConfig;
import com.bronx.telegram.notification.dto.channel.ChannelNotificationMessage;
import com.bronx.telegram.notification.dto.notification.NotificationMessage;
import com.bronx.telegram.notification.dto.notification.PersonalNotificationMessage;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.service.NotificationQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private final RabbitTemplate rabbitTemplate;


    @Override
    public void queuePersonalNotification(NotificationPersonal notification) {
        try {
            PersonalNotificationMessage message = PersonalNotificationMessage.builder()
                    .notificationId(notification.getId())
                    .partnerId(notification.getPartner().getId())
                    .subscriptionId(notification.getSubscription().getId())
                    .notificationType(notification.getNotificationType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .content(notification.getContent())
                    .priority(notification.getPriority())
                    .retryCount(notification.getRetryCount())
                    .queuedAt(notification.getQueuedAt())
                    .employeeId(notification.getEmployee().getId())
                    .telegramChatId(notification.getEmployee().getTelegramChatId())
                    .employeeEmail(notification.getEmployee().getEmail())
                    .employeeName(notification.getEmployee().getFullName())
                    .receivedAt(notification.getReceivedAt())
                    .location(notification.getLocation())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATION_EXCHANGE,
                    RabbitMqConfig.PERSONAL_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setPriority(
                                notification.getPriority().getValue()
                        );
                        return msg;
                    }
            );

            log.info("📤 Queued personal notification {} for employee {} (priority: {})",
                    notification.getId(),
                    notification.getEmployee().getEmployeeCode(),
                    notification.getPriority());

        } catch (Exception e) {
            log.error("❌ Failed to queue personal notification {}", notification.getId(), e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }

    @Override
    public void queueChannelNotification(NotificationChannel notification) {
        try {
            ChannelNotificationMessage message = ChannelNotificationMessage.builder()
                    .notificationId(notification.getId())
                    .partnerId(notification.getPartner().getId())
                    .subscriptionId(notification.getSubscription().getId())
                    .notificationType(notification.getNotificationType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .content(notification.getContent())
                    .priority(notification.getPriority())
                    .retryCount(notification.getRetryCount())
                    .queuedAt(notification.getQueuedAt())
                    .channelId(notification.getTelegramChannel().getId())
                    .chatId(notification.getTelegramChannel().getChatId())
                    .botId(notification.getTelegramChannel().getBot().getId())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATION_EXCHANGE,
                    RabbitMqConfig.CHANNEL_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setPriority(
                                notification.getPriority().getValue()
                        );
                        return msg;
                    }
            );

            log.info("📤 Queued channel notification {} for channel '{}' (priority: {})",
                    notification.getId(),
                    notification.getTelegramChannel().getChatName(),
                    notification.getPriority());

        } catch (Exception e) {
            log.error("❌ Failed to queue channel notification {}", notification.getId(), e);
            throw new RuntimeException("Failed to queue notification", e);
        }
    }


    @Override
    public void queueWebhookProcessing(Webhook webhook) {
        try {
            WebhookMessage message = WebhookMessage.builder()
                    .webhookId(webhook.getId())
                    .botId(webhook.getBot().getId())
                    .updateId(webhook.getUpdateId())
                    .chatId(webhook.getChatId())
                    .userId(webhook.getUserId())
                    .username(webhook.getUsername())
                    .messageType(webhook.getMessageType())
                    .command(webhook.getCommand())
                    .content(webhook.getContent())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.WEBHOOK_EXCHANGE,
                    RabbitMqConfig.WEBHOOK_ROUTING_KEY,
                    message
            );

            log.info("📤 Queued webhook {} for processing", webhook.getId());

        } catch (Exception e) {
            log.error("❌ Failed to queue webhook {}", webhook.getId(), e);
            throw new RuntimeException("Failed to queue webhook", e);
        }
    }

    /**
     * ✅ ENHANCEMENT: Requeue with exponential backoff
     */
    public void requeueForRetry(NotificationMessage message, long delaySeconds) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.RETRY_EXCHANGE,
                RabbitMqConfig.RETRY_ROUTING_KEY,
                message,
                msg -> {

                    msg.getMessageProperties().setDelayLong((long) (delaySeconds * 1000));
                    return msg;
                }
        );
    }
}
