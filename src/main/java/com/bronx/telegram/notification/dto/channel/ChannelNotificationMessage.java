package com.bronx.telegram.notification.dto.channel;

import com.bronx.telegram.notification.dto.notification.NotificationMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChannelNotificationMessage extends NotificationMessage {
    private Long channelId;
    private String chatId;
    private Long botId;
    private Boolean pinMessage;
    private Boolean disableNotification;
}
