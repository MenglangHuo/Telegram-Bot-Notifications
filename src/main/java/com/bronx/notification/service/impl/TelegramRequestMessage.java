package com.bronx.notification.service.impl;

import com.bronx.notification.dto.notification.NotificationMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.bronx.notification.utils.JsonParserUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TelegramRequestMessage {

    public TelegramMessageRequest buildTelegramRequest(
            Notification notification
    ) {
        TelegramMessageRequest.TelegramMessageRequestBuilder builder =
                TelegramMessageRequest.builder()
                        .chatId(notification.getChartId())
                        .type(notification.getType())
                        .parseMode(notification.getTelegramParseMode() != null ? notification.getTelegramParseMode() : TelegramParseMode.HTML);

        switch (notification.getType()) {
            case TEXT:
                builder.message(notification.getMessage());
                break;
            case PHOTO:
            case VIDEO:
            case AUDIO:
            case DOCUMENT:
                builder.url(notification.getUrl())
                        .caption(notification.getCaption() != null ? notification.getCaption() : notification.getMessage())
                        .message(notification.getMessage())
                        .parseMode(TelegramParseMode.HTML);
                if (notification.getType() == TelegramMessageType.AUDIO) {
                    if(notification.getMetaData() != null) {
                        JsonNode mediaMetaData=notification.getMetaData();
                        var title= JsonParserUtils.text(mediaMetaData,"title");
                        var performer=JsonParserUtils.text(mediaMetaData,"performer");
                        var iconUrl=JsonParserUtils.text(mediaMetaData,"iconUrl");
                        builder.title(title).performer(performer).audioIconUrl(iconUrl);
                    }

                }
                break;
        }

        return builder.build();
    }
}
