package com.bronx.telegram.notification.utils;

import com.bronx.telegram.notification.dto.channel.ChannelNotificationMessage;
import com.bronx.telegram.notification.dto.notification.PersonalNotificationMessage;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.model.enumz.TelegramParseMode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Lazy
@Component
public class TelegramRequestMessage {

    public TelegramMessageRequest buildPersonalTelegramRequest(
            NotificationPersonal personal,
            PersonalNotificationMessage message
    ) {

        TelegramMessageRequest.TelegramMessageRequestBuilder builder =
                TelegramMessageRequest.builder()
                        .chatId(personal.getEmployee().getTelegramChatId())
                        .messageType(message.getMediaType() != null ?
                                message.getMediaType() : TelegramMessageType.TEXT)
                        .parseMode(TelegramParseMode.HTML);

        switch (message.getMediaType() != null ? message.getMediaType() : TelegramMessageType.TEXT) {
            case TEXT:
                String textMessage;
                if(personal.isOwnCustom())
                    textMessage=personal.getMessage();
                else
                    textMessage = renderMessage(personal);
                builder.text(textMessage).parseMode(personal.getTelegramParseMode());
                break;

            case PHOTO:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildMediaCaption(personal, message))
                        .parseMode(TelegramParseMode.HTML);

                break;

            case VIDEO:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildMediaCaption(personal, message))
                        .parseMode(TelegramParseMode.HTML);
                break;

            case AUDIO:
                JsonNode mediaMetaData=personal.getMetaData();
                var title=TelegramJson.text(mediaMetaData,"title");
                var performer=TelegramJson.text(mediaMetaData,"performer");
//                var duration=TelegramJson.longVal(mediaMetaData,"duration");

                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildMediaCaption(personal, message))
                        .title(title)
                        .performer(performer)
                        .parseMode(TelegramParseMode.HTML);
                break;

            case DOCUMENT:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildMediaCaption(personal, message))
                        .parseMode(TelegramParseMode.HTML);
                break;

        }

        return builder.build();
    }


    public TelegramMessageRequest buildChannelMessageRequest(
            NotificationChannel notification,
            ChannelNotificationMessage message) {

        TelegramMessageRequest.TelegramMessageRequestBuilder builder =
                TelegramMessageRequest.builder()
                        .chatId(notification.getTelegramChannel().getChatId())
                        .messageType(message.getMediaType() != null ?
                                message.getMediaType() : TelegramMessageType.TEXT);

        switch (message.getMediaType() != null ? message.getMediaType() : TelegramMessageType.TEXT) {
            case TEXT:
                String textMessage;
                if(notification.isOwnCustom())
                    textMessage=notification.getMessage();
                else
                    textMessage = renderChannelMessage(notification);

                builder.text(textMessage).parseMode(notification.getTelegramParseMode());
                break;

            case PHOTO:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildChannelCaption(notification, message))
                        .parseMode(TelegramParseMode.HTML);
                break;

            case VIDEO:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildChannelCaption(notification, message))
                        .parseMode(TelegramParseMode.HTML);
                break;

            case AUDIO:
                JsonNode mediaMetaData=notification.getMetaData();
                var title=TelegramJson.text(mediaMetaData,"title");
                var performer=TelegramJson.text(mediaMetaData,"performer");

                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildChannelCaption(notification, message))
                        .title(title)
                        .performer(performer)
//                        .duration(282)
                        .parseMode(TelegramParseMode.HTML);
                break;

            case DOCUMENT:
                builder.fileUrl(message.getMediaUrl())
                        .fileId(message.getMediaFileId())
                        .caption(buildChannelCaption(notification, message))
                        .parseMode(TelegramParseMode.HTML);
                break;

        }

        return builder.build();
    }

    private String renderChannelMessage(NotificationChannel n) {
        StringBuilder message = new StringBuilder();

        if (n.getTitle() != null) {
            message.append("<b>").append(n.getTitle()).append("</b>\n\n");
        }

        message.append(n.getMessage());

        return message.toString();
    }

    private String renderMessage(NotificationPersonal notification) {
        return switch (notification.getNotificationType()) {
            case NotificationEventType.CUSTOM_EVENT -> renderCheckInMessage(notification);
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

        return String.format("""
        <b>%s</b>

        👤 <b>Employee:</b> %s
        📅 <b>Date:</b> %s
        🕒 <b>Time:</b> %s
        📍 <b>Location:</b> %s
        
        Have a productive day! 🚀
        """,
                n.getTitle(),
                employeeName,
                date,
                time,
                location
        );
    }

    private String buildMediaCaption(
            NotificationPersonal notification,
            PersonalNotificationMessage message) {

        if (message.getMediaCaption() != null && !message.getMediaCaption().isEmpty()) {
            return message.getMediaCaption();
        }

        // Build default caption
        StringBuilder caption = new StringBuilder();
        if (notification.getTitle() != null) {
            caption.append("<b>").append(notification.getTitle()).append("</b>\n\n");
        }
        if (notification.getMessage() != null) {
            caption.append(notification.getMessage());
        }

        return caption.toString();
    }

    private String buildChannelCaption(
            NotificationChannel notification,
            ChannelNotificationMessage message) {

        if (message.getMediaCaption() != null && !message.getMediaCaption().isEmpty()) {
            return message.getMediaCaption();
        }

        StringBuilder caption = new StringBuilder();
        if (notification.getTitle() != null) {
            caption.append("<b>").append(notification.getTitle()).append("</b>\n\n");
        }
        if (notification.getMessage() != null) {
            caption.append(notification.getMessage());
        }

        return caption.toString();
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


}
