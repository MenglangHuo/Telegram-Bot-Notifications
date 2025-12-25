package com.bronx.notification.dto.notification;

import com.bronx.notification.model.enumz.NotificationPriority;
import com.bronx.notification.model.enumz.NotificationStatus;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.fasterxml.jackson.databind.JsonNode;
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
    private Long id;
    private String chartId;
    private Long subscriptionId;
    private boolean isOwnCustom;
    private String botUsername;
    private String teleTemplateName;
    private TelegramMessageType type;
    private TelegramParseMode telegramParseMode=TelegramParseMode.HTML;
    private String url;
    private String caption;
    private JsonNode metaData;
    private NotificationStatus status;
    private String message;
    private NotificationPriority priority;
    private Integer retryCount;
    private String location;
    private Instant queuedAt;
    private Instant receivedAt;
    private Instant processingAt;
}
