package com.bronx.notification.service.impl;

import com.bronx.notification.configs.RabbitMqConfig;
import com.bronx.notification.dto.notification.MediaMetaData;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.NotificationMapper;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.entity.TelegramBot;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.TelegramBotRepository;
import com.bronx.notification.service.NotificationService;
import com.bronx.notification.service.TelegramTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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



    @Override
    @Transactional
    public void createAndQueueNotification(TelegramMessageRequest dto) {

        Notification notification = notificationMapper.toEntity(dto);

        if(dto.getTemplateName() != null) {
            TelegramTemplateResponse template = templateService.getByName(dto.getTemplateName());
            String renderText=renderTemplate(template.htmlContent(), dto.getVars());
            if (notification.getType() == TelegramMessageType.TEXT) {
                notification.setMessage(renderText);
            } else {
                notification.setMessage(renderText);
                notification.setUrl(dto.getUrl());
                notification.setType(dto.getType());
                if(dto.getTitle()!=null && dto.getPerformer()!=null){
                    MediaMetaData mediaMetaData=new MediaMetaData(dto.getTitle(),dto.getPerformer(),dto.getAudioIconUrl(),dto.getCaption());
                    notification.setMetaData(objectMapper.valueToTree(mediaMetaData));
                }
            }
            notification.setOwnCustom(false);
            notification.setTelegramParseMode(TelegramParseMode.HTML);
        }else{
            if(dto.isOwnCustom() && dto.getType()!=TelegramMessageType.TEXT){
                throw new BusinessException("Invalid! if own custom true therefore message type must be type TEXT");
            }else{
                if(dto.isOwnCustom()){
                    notification.setMessage(dto.getMessage());
                    notification.setOwnCustom(true);
                    notification.setTelegramParseMode(dto.getParseMode());
                }else{
                    notification.setCaption(dto.getCaption());
                    notification.setTelegramParseMode(dto.getParseMode());
                    notification.setOwnCustom(false);
                    notification.setUrl(dto.getUrl());
                    notification.setType(dto.getType());
                    if(dto.getTitle()!=null && dto.getPerformer()!=null){
                            MediaMetaData mediaMetaData=new MediaMetaData(dto.getTitle(),dto.getPerformer(),dto.getAudioIconUrl(),dto.getCaption());
                            notification.setMetaData(objectMapper.valueToTree(mediaMetaData));
                    }
                }

            }
        }

        TelegramBot bot = telegramBotRepository.findByBotUsername(dto.getBotUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Telegram Bot not found"));
        notification.setSubscription(bot.getSubscription());
        notification.setChartId(dto.getChatId());
        Notification saved = notificationRepository.save(notification);

        log.info("send to queue.....");
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.NOTIFICATION_EXCHANGE,
                RabbitMqConfig.ROUTING_KEY,
                notificationMapper.toResponse(saved),
                msg -> {
                    msg.getMessageProperties().setPriority(
                            notification.getPriority().getValue()
                    );
                    return msg;
                }
        );


    }


    private String renderTemplate(String content, Map<String, String> vars) {
        // Simple placeholder replacement, e.g., using String replacement or a template engine like Thymeleaf/FreeMarker
        // For simplicity, manual replacement
        if (vars == null) return content;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
}
