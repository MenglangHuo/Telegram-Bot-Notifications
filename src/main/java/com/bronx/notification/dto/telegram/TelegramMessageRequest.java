package com.bronx.notification.dto.telegram;

import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageRequest {

    @NotBlank(message = "Bot Name is required")
    private String botName;

    @NotBlank(message = "Chat ID is required")
    private String chatId;

    @NotNull(message = "Message type is required")
    private TelegramMessageType messageType;

    // Text message fields
    private String text;
    private TelegramParseMode parseMode;

    private MediaRequest mediaRequest;


    public void validate() {
        if (messageType == TelegramMessageType.TEXT &&
                (text == null || text.trim().isEmpty())) {
            throw new IllegalArgumentException("Text is required for TEXT message type");
        }

        if ((messageType == TelegramMessageType.PHOTO ||
                messageType == TelegramMessageType.VIDEO ||
                messageType == TelegramMessageType.AUDIO ||
                messageType == TelegramMessageType.DOCUMENT)) {
            throw new IllegalArgumentException(
                    "Either fileUrl or fileData is required for media messages");
        }
    }
}
