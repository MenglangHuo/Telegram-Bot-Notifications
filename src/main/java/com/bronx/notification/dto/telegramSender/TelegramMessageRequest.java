package com.bronx.notification.dto.telegramSender;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramMessageRequest {

    @NotBlank(message = "Bot UserName is required")
    private String botUsername;

    @NotBlank(message = "Chat ID is required")
    private String chatId;

    @NotNull(message = "Message type is required")
    @Builder.Default
    private TelegramMessageType type=TelegramMessageType.TEXT;

    @Builder.Default
    private boolean isOwnCustom = false;

    @Builder.Default
        private TelegramParseMode parseMode=TelegramParseMode.HTML;

    private String url;
    private String templateName;
    private String caption;
    private String message;
    private Map<String, String> vars;

    //metadata of audio
    private String title;
    private String performer;
    private byte[] fileData;
    private String fileName;
    private String audioIconUrl;


}
