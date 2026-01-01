package com.bronx.notification.service.impl;

import com.bronx.notification.configs.RabbitMqConfig;
import com.bronx.notification.dto.creditUsage.CreditOperationResult;
import com.bronx.notification.dto.notification.MediaMetaData;
import com.bronx.notification.dto.notification.NotificationMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.InsufficientCreditsException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.NotificationMapper;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.model.entity.TelegramBot;
import com.bronx.notification.model.enumz.NotificationPriority;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.TelegramBotRepository;
import com.bronx.notification.service.NotificationService;
import com.bronx.notification.service.TelegramTemplateService;
import com.bronx.notification.service.impl.creditMessage.CreditService;
import com.bronx.notification.service.impl.creditMessage.CreditServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for creating and queueing notifications.
 * Validates credits BEFORE queueing to prevent processing messages without
 * sufficient credits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final TelegramTemplateService templateService;
    private final ObjectMapper objectMapper;
    private final TelegramBotRepository telegramBotRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CreditServiceFactory creditServiceFactory;


    @Override
    @Transactional
    public NotificationMessage createAndQueueNotification(TelegramMessageRequest dto) {
        // 1. Validate bot exists and get subscription
        TelegramBot bot = telegramBotRepository.findByBotUsername(dto.getBotUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Telegram Bot not found: " + dto.getBotUsername()));
        Subscription subscription = bot.getSubscription();
        if (subscription == null || !subscription.isValid()) {
            throw new BusinessException("Bot's subscription is not active");
        }

        // 2. Check and get credit type (redis or db direct)
        CreditService creditService = creditServiceFactory.getCreditService();
        int creditCost = 1; // Default cost per message

        // 3. Create notification entity
        Notification notification = notificationMapper.toEntity(dto);
        notification.setSubscription(subscription);

        notification.setChartId(dto.getChatId());
        notification.setCreditCost(creditCost);
        notification.setPriority(dto.getPriority() != null ? dto.getPriority() : NotificationPriority.NORMAL);
        // 4. Handle template or direct message
        if (dto.getTemplateName() != null) {
            TelegramTemplateResponse template = templateService.getByName(dto.getTemplateName());
            String renderText = renderTemplate(template.htmlContent(), dto.getVars());
            if (notification.getType() == TelegramMessageType.TEXT) {
                notification.setMessage(renderText);
            } else {
                notification.setMessage(renderText);
                notification.setUrl(dto.getUrl());
                notification.setType(dto.getType());
                if (dto.getTitle() != null && dto.getPerformer() != null) {
                    MediaMetaData mediaMetaData = new MediaMetaData(
                            dto.getTitle(),
                            dto.getPerformer(),
                            dto.getAudioIconUrl(),
                            dto.getCaption());
                    notification.setMetaData(objectMapper.valueToTree(mediaMetaData));
                }
            }
            notification.setOwnCustom(false);
            notification.setTelegramParseMode(TelegramParseMode.HTML);
        } else {
            if (dto.isOwnCustom() && dto.getType() != TelegramMessageType.TEXT) {
                // Rollback credit if validation fails
//                creditService.rollbackCredit(subscription.getId(), creditCost, creditResult.getTrackingId());
                throw new BusinessException("Invalid! if own custom true therefore message type must be type TEXT");
            }
            if (dto.isOwnCustom()) {
                notification.setMessage(dto.getMessage());
                notification.setOwnCustom(true);
                notification.setTelegramParseMode(dto.getParseMode());
            } else {
                notification.setCaption(dto.getCaption());
                notification.setTelegramParseMode(dto.getParseMode());
                notification.setOwnCustom(false);

                notification.setUrl(dto.getUrl());
                notification.setType(dto.getType());
                if (dto.getTitle() != null && dto.getPerformer() != null) {
                    MediaMetaData mediaMetaData = new MediaMetaData(
                            dto.getTitle(),
                            dto.getPerformer(),
                            dto.getAudioIconUrl(),
                            dto.getCaption());
                    notification.setMetaData(objectMapper.valueToTree(mediaMetaData));
                }
            }
        }
        // 5. Save notification
        Notification saved = notificationRepository.save(notification);



        CreditOperationResult creditResult = creditService.checkAndDecrementCredit(
                subscription.getId(),
                creditCost,
                saved.getId() // Notification ID not yet created
        );
        if (!creditResult.isSuccess()) {
            log.warn("Insufficient credits for subscription {}: {}",
                    subscription.getId(), creditResult.getMessage());
            throw new InsufficientCreditsException(
                    subscription.getId(),
                    creditCost,
                    creditResult.getRemainingCredits());
        }
        log.debug("Credits reserved for subscription {}, remaining: {}",
                subscription.getId(), creditResult.getRemainingCredits());


        log.info("Notification {} created, sending to queue", saved.getId());
        // 6. Queue the notification
        NotificationMessage message = notificationMapper.toResponse(saved);


        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY,
                message,
                msg -> {
                    msg.getMessageProperties().setPriority(
                            notification.getPriority().getValue());
                    return msg;
                });
        return message;
    }
    private String renderTemplate(String content, Map<String, String> vars) {
        if (vars == null)
            return content;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
}
