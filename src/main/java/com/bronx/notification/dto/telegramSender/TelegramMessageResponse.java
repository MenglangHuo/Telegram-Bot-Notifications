package com.bronx.notification.dto.telegramSender;


import com.bronx.notification.model.enumz.TelegramMessageType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageResponse {

    private boolean success;
    private String messageId;
    private String chatId;
    private TelegramMessageType messageType;
    private String errorMessage;
    private Integer errorCode;
    private Instant timestamp;
    public static TelegramMessageResponse success(String messageId, String chatId,
                                                  TelegramMessageType type) {
        return TelegramMessageResponse.builder()
                .success(true)
                .messageId(messageId)
                .chatId(chatId)
                .messageType(type)
                .timestamp(Instant.ofEpochSecond(System.currentTimeMillis()))
                .build();
    }

    public static TelegramMessageResponse error(String errorMessage, Integer errorCode,
                                                String chatId, TelegramMessageType type) {
        return TelegramMessageResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .chatId(chatId)
                .messageType(type)
                .timestamp(Instant.ofEpochSecond(System.currentTimeMillis()))
                .build();
    }
}
