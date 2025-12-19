package com.bronx.telegram.notification.dto.checkIn;

import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
import com.bronx.telegram.notification.model.enumz.ParseMode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {

    private String employeeCode;
    private String organizationCode;
    private NotificationEventType notificationType;
    private String checkInTime;

    private String title;
    private String message;
    private String location;
    private String method;
    private JsonNode data;
    private ParseMode parseMode; //markdown or html
    private Boolean isOwnCustom;
    private NotificationPriority priority = NotificationPriority.NORMAL;
}

