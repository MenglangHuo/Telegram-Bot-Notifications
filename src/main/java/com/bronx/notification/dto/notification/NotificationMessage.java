package com.bronx.notification.dto.notification;

import com.bronx.notification.model.enumz.NotificationPriority;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder
public class NotificationMessage implements Serializable {
    private Long notificationId;
    private Long partnerId;
    private Long subscriptionId;

    private boolean isOwnCustom;
    private TelegramParseMode telegramParseMode=TelegramParseMode.HTML;
    private String title;
    private String message;
    private NotificationPriority priority;
    private Integer retryCount;
    private String location;

    private Instant queuedAt;
    private Instant receivedAt;
    private Instant processingAt;

    // Media fields
    private TelegramMessageType mediaType;
    private String mediaUrl;
    private String mediaCaption;


}
