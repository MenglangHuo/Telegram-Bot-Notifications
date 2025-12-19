package com.bronx.telegram.notification.dto.checkIn;

import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
import com.bronx.telegram.notification.model.enumz.ParseMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequest {
    private String companyCode;
    private String channelName;
    private String type;
    private String title;
    private String message;
    private ParseMode parseMode;
    private NotificationEventType  eventType;//alert or announcement
    private NotificationPriority priority = NotificationPriority.NORMAL;
}

