package com.bronx.telegram.notification.consumer;

import com.bronx.telegram.notification.configs.RabbitMqConfig;
import com.bronx.telegram.notification.dto.channel.ChannelNotificationMessage;
import com.bronx.telegram.notification.dto.notification.NotificationMessage;
import com.bronx.telegram.notification.dto.notification.PersonalNotificationMessage;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.ChannelStatus;
import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
import com.bronx.telegram.notification.model.enumz.NotificationStatus;
import com.bronx.telegram.notification.repository.NotificationChannelRepository;
import com.bronx.telegram.notification.repository.NotificationPersonalRepository;
import com.bronx.telegram.notification.service.NotificationQueueService;
import com.bronx.telegram.notification.service.TelegramBotService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationPersonalRepository personalRepository;
    private final NotificationChannelRepository channelRepository;
    private final TelegramBotService telegramBotService;
    private final NotificationQueueService queueService;

    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_PERSONAL_QUEUE,
            concurrency = "3-10",
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

            // Render message based on notification type
            String telegramMessage = renderMessage(notification);

            // Send via Telegram
        log.info("chart id: {}",notification.getEmployee().getTelegramChatId());
            boolean success = telegramBotService.sendPersonalMessage(
                    bot.getId(),
                    notification.getEmployee().getTelegramChatId(),
                    telegramMessage,
                    notification.getPriority() == NotificationPriority.URGENT
            );

            if (success) {
                Instant now = Instant.now();
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(now);
                notification.setProcessingDurations(Duration.between(startTime, now).toMillis());

                // Update bot activity
//                bot.setMessagesSentToday(bot.getMessagesSentToday() + 1);
//                bot.setLastActivityAt(now);

//                log.info("✅ Successfully sent personal notification {} in {}ms",
//                        message.getNotificationId(),
//                        notification.getProcessingAt());
            } else {
                handleFailure(notification, "Failed to send message via Telegram");
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

            // Get notification settings from channel
            boolean pinMessage = false;
            boolean disableNotification = false;

            if (channel.getNotificationSettings() != null) {
                JsonNode settings = channel.getNotificationSettings();
                pinMessage = settings.has("auto_pin") &&
                        settings.get("auto_pin").asBoolean();
                disableNotification = settings.has("disable_notification") &&
                        settings.get("disable_notification").asBoolean();
            }

            // Override for urgent notifications
            if (notification.getPriority() == NotificationPriority.URGENT) {
                pinMessage = true;
                disableNotification = false;
            }

            // Render message
            String telegramMessage = formatChannelMessage(notification);

            // Send via Telegram
            boolean success = telegramBotService.sendChannelMessage(
                    channel.getBot().getId(),
                    channel.getChatId(),
                    telegramMessage,
                    pinMessage,
                    disableNotification
            );

            if (success) {
                Instant now = Instant.now();
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(now);
                notification.setProcessingDurations(Duration.between(startTime, now).toMillis());

                // Update channel activity
//                channel.setLastMessageAt(now);

//                log.info("✅ Successfully sent channel notification {} to '{}' in {}ms",
//                        message.getNotificationId(),
//                        channel.getChatName(),
//                        notification.getProcessingDurations());
            } else {
                handleFailure(notification, "Failed to send message to channel");
            }

            channelRepository.save(notification);

        } catch (Exception e) {
            log.error("❌ Error processing channel notification {}: {}",
                    message.getNotificationId(), e.getMessage(), e);
            handleRetry(message);
        }
    }


    private String renderMessage(NotificationPersonal notification) {
        return switch (notification.getNotificationType()) {
            case NotificationEventType.CHECK_IN -> renderCheckInMessage(notification);
            case NotificationEventType.SYSTEM_ALERT -> renderAlertMessage(notification);
            case NotificationEventType.TEAM_ANNOUNCEMENT -> renderAnnouncementMessage(notification);
            default -> renderGenericMessage(notification);
        };
    }

    private String renderCheckInMessage(NotificationPersonal n) {
        String employeeName = n.getEmployee().getFullName();

        // Keep original instant, just format it
        ZoneId zone = ZoneId.systemDefault(); // no offset change

        String date = DateTimeFormatter
                .ofPattern("yyyy-MM-dd")
                .withZone(zone)
                .format(n.getReceivedAt());

        String time = DateTimeFormatter
                .ofPattern("hh:mm:ss a")
                .withZone(zone)
                .format(n.getReceivedAt());

        String location = n.getLocation() != null ? n.getLocation() : "N/A";
        String method = n.getMethod() != null ? n.getMethod() : "N/A";

        return String.format("""
        <b>%s</b>

        👤 <b>Employee:</b> %s
        
        📅 <b>Date:</b> %s
        
        🕒 <b>Time:</b> %s
        
        📍 <b>Location:</b> %s
        
        🔐 <b>Method:</b> %s

        Have a productive day! 🚀
        """,
                n.getTitle(),
                employeeName,
                date,
                time,
                location,
                method
        );
    }



    private String renderAlertMessage(NotificationPersonal n) {
        return String.format("""
            🚨 <b>%s</b>
            
            %s
            
            <i>Priority: %s</i>
            """,
                n.getTitle(),
                n.getMessage(),
                n.getPriority()
        );
    }
    private String renderAnnouncementMessage(NotificationPersonal n) {
        return String.format("""
            📢 <b>%s</b>

            %s
            """,
                n.getTitle(),
                n.getMessage()
        );
    }

    private String renderGenericMessage(NotificationPersonal n) {
        return String.format("""
            <b>%s</b>

            %s
            """,
                n.getTitle() != null ? n.getTitle() : "Notification",
                n.getMessage()
        );
    }

    private String formatChannelMessage(NotificationChannel n) {
        StringBuilder message = new StringBuilder();

        // Add title
        if (n.getTitle() != null) {
            message.append("<b>").append(n.getTitle()).append("</b>\n\n");
        }

        // Add message
        message.append(n.getMessage());

        // Add scope information
//        String scope = getScopeInfo(n);
//        if (scope != null) {
//            message.append("\n\n<i>").append(scope).append("</i>");
//        }

        return message.toString();
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
