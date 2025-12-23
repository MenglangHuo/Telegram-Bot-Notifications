package com.bronx.telegram.notification.consumer;
import com.bronx.telegram.notification.configs.RabbitMqConfig;
import com.bronx.telegram.notification.dto.channel.ChannelNotificationMessage;
import com.bronx.telegram.notification.dto.notification.NotificationMessage;
import com.bronx.telegram.notification.dto.notification.PersonalNotificationMessage;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.*;
import com.bronx.telegram.notification.repository.NotificationChannelRepository;
import com.bronx.telegram.notification.repository.NotificationPersonalRepository;
import com.bronx.telegram.notification.service.NotificationQueueService;
import com.bronx.telegram.notification.service.TelegramBotService;
import com.bronx.telegram.notification.service.telegramMessage.TelegramMessageSender;
import com.bronx.telegram.notification.utils.TelegramRequestMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationPersonalRepository personalRepository;
    private final NotificationChannelRepository channelRepository;
    private final TelegramBotService telegramBotService;
    private final NotificationQueueService queueService;
    private final TelegramMessageSender messageSender;
    private final TelegramRequestMessage telegramRequestMessage;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_PERSONAL_QUEUE,
            concurrency = "5-10",
            ackMode = "AUTO"
    )
    @Transactional
    public void processPersonalNotification(PersonalNotificationMessage message) {
        log.info("Processing personal notification {}", message.getNotificationId());
        Instant startTime = Instant.now();

        try {
            NotificationPersonal notification = personalRepository
                    .findWithEmployee(message.getNotificationId())
                    .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

            //  Update status with processing timestamp
            notification.setStatus(NotificationStatus.PROCESSING);
            notification.setProcessingAt(startTime);

            personalRepository.save(notification);

            String chatId = notification.getEmployee().getTelegramChatId();

            if (chatId == null || chatId.isEmpty()) {
                handleFailure(notification, "Employee has no Telegram chat ID "+notification.getEmployee().getId());
                log.error("❌ Employee {} has no telegram_chat_id",
                        notification.getEmployee().getEmployeeCode());
                return;
            }
            // Get bot for subscription
            TelegramBot bot = getBotForSubscription(notification.getSubscription());
            if (bot == null || !bot.isHealthy()) {
                handleFailure(notification, "No healthy bot available for subscription");
                return;
            }

            // Build Telegram request
            TelegramMessageRequest telegramMessage=telegramRequestMessage.buildPersonalTelegramRequest(notification,message);

            // Send via enhanced sender
            TelegramMessageResponse response = messageSender.sendMessage(
                    bot.getId(),
                    telegramMessage
            );

            // Send via Telegram
            log.info("chart id: {}",notification.getEmployee().getTelegramChatId());
            if (response.isSuccess()) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
                notification.setProcessingDurations(
                        Duration.between(startTime, Instant.now()).toMillis());
                notification.setTelegramMessageId(response.getMessageId());

                log.info("✅ Personal notification {} sent successfully",
                        notification.getId());
            } else {
                handleFailure(notification, response.getErrorMessage());
            }

            personalRepository.save(notification);

        } catch (Exception e) {
            log.error("❌ Error processing personal notification {}: {}",
                    message.getNotificationId(), e.getMessage(), e);
            handleRetry(message);
        }
    }

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_CHANNEL_QUEUE,
            concurrency = "3-10",
            ackMode = "AUTO"
    )
    @Transactional
    public void processChannelNotification(ChannelNotificationMessage message) {
        log.info("Processing channel notification {}", message.getNotificationId());
        Instant startTime = Instant.now();

        try {
            NotificationChannel notification = channelRepository
                    .findWithChannel(message.getNotificationId())
                    .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

            // Update status
            notification.setStatus(NotificationStatus.PROCESSING);
            notification.setProcessingAt(startTime);
            channelRepository.save(notification);

            TelegramChannel channel = notification.getTelegramChannel();

            // Validate channel is active
            if (channel.getChannelStatus() != ChannelStatus.ACTIVE) {
                handleFailure(notification, "Channel is not active");
                return;
            }

            // Build request
            TelegramMessageRequest request =telegramRequestMessage.buildChannelMessageRequest(notification,message);

            // Send message
            TelegramMessageResponse response = messageSender.sendMessage(
                    channel.getBot().getId(),
                    request
            );

            if (response.isSuccess()) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
                notification.setProcessingDurations(
                        Duration.between(startTime, Instant.now()).toMillis());
                notification.setTelegramMessageId(response.getMessageId());

                log.info("✅ Channel notification {} sent successfully",
                        notification.getId());
            } else {
                handleFailure(notification, response.getErrorMessage());
            }

            channelRepository.save(notification);

        } catch (Exception e) {
            log.error("❌ Error processing channel notification {}: {}",
                    message.getNotificationId(), e.getMessage(), e);
            handleRetry(message);
        }
    }

    private TelegramBot getBotForSubscription(Subscription subscription) {
        return telegramBotService.getBotForSubscription(subscription.getId());
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

            queueService.requeueForRetry(message, delaySeconds);

            log.info("⏳ Requeued notification {} for retry attempt {} (delay: {}s)",
                    message.getNotificationId(),
                    message.getRetryCount(),
                    delaySeconds);
        } else {
            log.error("❌ Max retries exceeded for notification {}",
                    message.getNotificationId());
        }
    }

}
