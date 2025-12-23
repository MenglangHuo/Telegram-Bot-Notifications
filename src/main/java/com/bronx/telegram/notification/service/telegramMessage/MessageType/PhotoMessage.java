package com.bronx.telegram.notification.service.telegramMessage.MessageType;

import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.model.enumz.TelegramParseMode;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import com.bronx.telegram.notification.service.telegramMessage.AbstractTelegramMessageStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class PhotoMessage extends AbstractTelegramMessageStrategy {

    public PhotoMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl, telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) {
       try{
           request.validate();
           if(request.getFileUrl()==null){
                return this.sendPhotoByUpload(request);
           }else{
                return this.sendPhotoByUrl(request);
           }
       } catch (IOException e) {
           throw new RuntimeException(e);
       } catch (TelegramApiException e) {
           throw new RuntimeException(e);
       }
    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.PHOTO;
    }

    private TelegramMessageResponse sendPhotoByUrl(TelegramMessageRequest request) throws TelegramApiException, IOException {

        ObjectNode json=buildBasePayload(request);
        json.put("photo",request.getFileUrl());

        if(request.getCaption()!=null){
            String caption=truncateText(request.getCaption(), MAX_CAPTION_LENGTH,"Caption");
            json.put("caption",caption);
        }

        String payload= objectMapper.writeValueAsString(json);
        RequestBody body=RequestBody.create(payload,JSON_MEDIA_TYPE);
        Request httpRequest=new Request.Builder()
                .url(apiUrl + "/sendPhoto")
                .post(body)
                .build();
       try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
           return handleResponse(response, request);
       }
    }
    private TelegramMessageResponse sendPhotoByUpload(TelegramMessageRequest request){
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("chat_id", request.getChatId());
        builder.addFormDataPart("photo",request.getFileName()!=null?request.getFileName():"photo.jpg",
                RequestBody.create(request.getFileData(), MediaType.parse(request.getMimeType()!=null?request.getMimeType():"application/octet-stream")));

        if(request.getCaption()!=null){
            String caption = truncateText(request.getCaption(), MAX_CAPTION_LENGTH, "Caption");
            builder.addFormDataPart("caption", caption);
            if (request.getParseMode() != null &&
                    request.getParseMode() != TelegramParseMode.NONE) {
                builder.addFormDataPart(
                        "caption_parse_mode",
                        request.getParseMode().getValue()
                );
            }


        }
        RequestBody body=builder.build();
        Request httpRequest=new Request.Builder()
                .url(apiUrl+"/sendPhoto")
                .post(body)
                .build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
