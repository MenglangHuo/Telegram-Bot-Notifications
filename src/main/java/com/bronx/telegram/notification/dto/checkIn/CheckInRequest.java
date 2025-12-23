package com.bronx.telegram.notification.dto.checkIn;

import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.model.enumz.TelegramParseMode;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {

    private String employeeCode;
    private String organizationCode;

    private Boolean isOwnCustom=true;
    //HTML OR MARKDOWN
    private TelegramParseMode telegramParseMode;

    //ownCustom =false
    private NotificationEventType notificationType;
    private String checkInTime;
    private String title;
    private String message;
    private String location;

    private NotificationPriority priority = NotificationPriority.NORMAL;

    // Media fields
    private TelegramMessageType mediaType;
    private String mediaUrl;
    private String mediaFileId; // For reusing uploaded files
    private String mediaCaption;
    private String fileName;
    private String fileData;

    private String performer;
    private Integer duration;

}

