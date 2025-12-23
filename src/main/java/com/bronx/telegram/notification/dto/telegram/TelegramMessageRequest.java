package com.bronx.telegram.notification.dto.telegram;

import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.model.enumz.TelegramParseMode;
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

    // Basic fields
    @NotBlank(message = "Chat ID is required")
    private String chatId;

    @NotNull(message = "Message type is required")
    private TelegramMessageType messageType;

    // Text message fields
    private String text;
    private TelegramParseMode parseMode;

    // Media fields
    private String fileUrl;          // URL to file
    private String fileId;          // Telegram file_id for reuse
    private byte[] fileData;         // Raw file bytes
    private String fileName;         // File name
    private String caption;          // Caption for media
    private String mimeType;//

    //metadata of audio
    private String title;
    private String performer;
    private Integer duration;




    // Additional options
    private Boolean disableNotification;
    private Integer replyToMessageId;
    private Boolean disableWebPagePreview;

    // Keyboard options
    private JsonNode replyMarkup;


    public void validate() {
        if (messageType == TelegramMessageType.TEXT &&
                (text == null || text.trim().isEmpty())) {
            throw new IllegalArgumentException("Text is required for TEXT message type");
        }

        if ((messageType == TelegramMessageType.PHOTO ||
                messageType == TelegramMessageType.VIDEO ||
                messageType == TelegramMessageType.AUDIO ||
                messageType == TelegramMessageType.DOCUMENT) &&
                (fileUrl == null && fileData == null)) {
            throw new IllegalArgumentException(
                    "Either fileUrl or fileData is required for media messages");
        }
    }
}
