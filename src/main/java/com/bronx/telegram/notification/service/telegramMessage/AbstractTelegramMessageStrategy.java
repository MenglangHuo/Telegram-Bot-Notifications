package com.bronx.telegram.notification.service.telegramMessage;

import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Response;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTelegramMessageStrategy implements TelegramMessageStrategy {
    protected final String apiUrl;
    protected final TelegramBotClient telegramBotClient;
    protected final ObjectMapper objectMapper;

    protected static final int MAX_CAPTION_LENGTH = 1024;
    protected static final int MAX_TEXT_LENGTH = 4096;
    protected static final MediaType JSON_MEDIA_TYPE =
            MediaType.get("application/json; charset=utf-8");

    @Override
    public boolean supports(TelegramMessageType messageType) {
        return getMessageType() == messageType;
    }

    protected ObjectNode buildBasePayload(TelegramMessageRequest request) throws TelegramApiException {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("chat_id",request.getChatId());
//        if (request.getReplyToMessageId() != null) {
//            json.put("reply_to_message_id", request.getReplyToMessageId());
//        }

        if (request.getReplyMarkup() != null) {
            json.set("reply_markup", request.getReplyMarkup());
        }
        return json;
    }

    protected String truncateText(String text, int maxLength,String fieldName) {
        if (text != null && text.length() > maxLength) {
            log.warn("⚠️ {} truncated from {} to {} chars",
                    fieldName, text.length(), maxLength);
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    protected TelegramMessageResponse handleResponse(Response response,TelegramMessageRequest request) throws IOException {
        if(response.body() == null){
            log.error("❌ Empty response body");
            return TelegramMessageResponse.error(
                    "Empty response from Telegram",
                    response.code(),
                    request.getChatId(),
                    request.getMessageType()
            );
        }

        String responseBody=response.body().string();
        JsonNode result=objectMapper.readTree(responseBody);

        if(result.get("ok").asBoolean(false)){
            String messageId=result.get("result").get("message_id").asText();
            log.info("✅ {} message sent successfully to {} (msg_id: {})",
                    request.getMessageType(), request.getChatId(), messageId);

            return TelegramMessageResponse.success(
                    messageId,
                    request.getChatId(),
                    request.getMessageType()
            );
        }else{
            String errorMessage=result.get("description").asText();
            int errorCode=result.get("error_code").asInt();

            log.error("❌ Failed to send {} message to {}: {} (code: {})",
                    request.getMessageType(), request.getChatId(), errorMessage, errorCode);

            return TelegramMessageResponse.error(
                    errorMessage,
                    errorCode,
                    request.getChatId(),
                    request.getMessageType()
            );
        }

    }

    protected void handleTelegramError(Integer errorCode, String errorMessage)
            throws TelegramApiException {

        if (errorCode == null) return;

        switch (errorCode) {
            case 400:
                if (errorMessage.contains("chat not found")) {
                    throw new TelegramApiException("Chat not found or bot was blocked "+ errorCode);
                } else if (errorMessage.contains("message is too long")) {
                    throw new TelegramApiException("Message exceeds maximum length "+ errorCode);
                }
                break;
            case 403:
                throw new TelegramApiException("Bot was blocked by user "+ errorCode);
            case 429:
                throw new TelegramApiException("Rate limit exceeded "+ errorCode);
        }
    }


}
