package com.bronx.notification.consumer;

import com.bronx.notification.configs.RabbitMqConfig;
import com.bronx.notification.dto.notification.NotificationMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.entity.TelegramBot;
import com.bronx.notification.model.enumz.NotificationStatus;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.TelegramBotRepository;
import com.bronx.notification.service.impl.TelegramRequestMessage;
import com.bronx.notification.service.impl.telegramMessage.TelegramMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationRepository notificationRepository;
    private final TelegramBotRepository telegramBotRepository; // Add this
    private final RabbitTemplate rabbitTemplate;
    private final TelegramMessageSender messageSender;
    private final TelegramRequestMessage telegramRequestMessage; // Updated to handle unified

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_QUEUE,
            concurrency = "5-10",
            ackMode = "AUTO"
    )
    @Transactional
    public void processNotification(NotificationMessage message) { // Unified message type
        log.info("Processing notification {}", message.getId());
        Instant startTime = Instant.now();

        try {
            Notification notification = notificationRepository.findById(message.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

            // Update status
            notification.setStatus(NotificationStatus.PROCESSING);
            notification.setProcessingAt(startTime);
            notificationRepository.save(notification);

            // Query bot token by botUsername
            TelegramBot bot = telegramBotRepository.findByBotUsername(notification.getBotUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Bot not found: " + notification.getBotUsername()));

            if (!bot.isHealthy()) {
                handleFailure(notification, "Bot is not healthy");
                return;
            }

            // Build request (update TelegramRequestMessage to handle unified Notification)
            TelegramMessageRequest request = telegramRequestMessage.buildTelegramRequest(notification);

            // Send
            TelegramMessageResponse response = messageSender.sendMessage(
                    bot.getId(), // Or bot.getBotToken() if sender uses token
                    request
            );

            if (response.isSuccess()) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
                notification.setTelegramMessageId(response.getMessageId());
                log.info("✅ Notification {} sent successfully", notification.getId());
            } else {
                handleFailure(notification, response.getErrorMessage());
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("❌ Error processing notification {}: {}", message.getId(), e.getMessage(), e);
            handleRetry(message);
        }
    }

    private void handleFailure(Notification notification, String reason) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailedAt(Instant.now());
        notification.setFailureReason(reason);

        log.error("Notification {} failed: {}", notification.getId(), reason);
    }

    private void handleRetry(NotificationMessage message) {
        if (message.getRetryCount() < 3) {
            message.setRetryCount(message.getRetryCount() + 1);

            // Exponential backoff
            long delaySeconds = (long) Math.pow(2, message.getRetryCount()) * 5;

            requeueForRetry(message, delaySeconds);

            log.info("⏳ Requeued notification {} for retry attempt {} (delay: {}s)",
                    message.getId(),
                    message.getRetryCount(),
                    delaySeconds);
        } else {
            log.error("❌ Max retries exceeded for notification {}",
                    message.getId());
        }
    }
    private void requeueForRetry(NotificationMessage message, long delaySeconds) {
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
