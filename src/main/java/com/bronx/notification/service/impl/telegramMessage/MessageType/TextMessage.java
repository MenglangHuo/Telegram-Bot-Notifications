package com.bronx.notification.service.impl.telegramMessage.MessageType;

import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.model.enumz.TelegramParseMode;
import com.bronx.notification.service.impl.TelegramBotClient;
import com.bronx.notification.service.impl.telegramMessage.AbstractTelegramMessageStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Slf4j
public class TextMessage extends AbstractTelegramMessageStrategy {


    public TextMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl,telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try{
//            request.validate();
            String text=truncateText(request.getMessage(), MAX_TEXT_LENGTH,"Text");
            ObjectNode json=buildBasePayload(request);
            json.put("text",text);
            if(request.getType()!=null &&request.getParseMode() != TelegramParseMode.NONE) {
                json.put("parse_mode", request.getParseMode().getValue());
            }
            String payload=objectMapper.writeValueAsString(json);
            RequestBody body=RequestBody.create(payload,JSON_MEDIA_TYPE);

            Request httpRequest=new Request.Builder()
                    .url(apiUrl + "/sendMessage")
                    .post(body)
                    .build();
            try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
                return handleResponse(response, request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }catch (RuntimeException e){
            log.error("❌ Error sending text message", e);
            return TelegramMessageResponse.error(
                    e.getMessage(),
                    null,
                    request.getChatId(),
                    request.getType()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.TEXT;
    }
}
