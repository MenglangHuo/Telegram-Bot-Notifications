package com.bronx.telegram.notification.dto.notification;

import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
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
    private Long notificationId;
    private Long partnerId;
    private Long subscriptionId;
    private Long organizationId;
    private Long divisionId;
    private Long departmentId;

    private String title;
    private String message;
    private JsonNode content;
    private NotificationPriority priority;
    private Integer retryCount;
    private String location;
    private String method;
   private NotificationEventType notificationType;
    private Instant queuedAt;
    private Instant receivedAt;
    private Instant processingAt;

}
