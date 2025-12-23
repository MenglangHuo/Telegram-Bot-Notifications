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

public class VideoMessage extends AbstractTelegramMessageStrategy {


    public VideoMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl, telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try{
            request.validate();
            if(request.getFileUrl()==null){
                return this.sendByUpload(request);
            }else{
                return this.sendVideoByUrl(request);
            }
        }catch (RuntimeException e){
            throw new TelegramApiException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.VIDEO;
    }

    private TelegramMessageResponse sendByUpload(TelegramMessageRequest request) {
        // Implementation for sending video by URL
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("chat_id",request.getChatId());
        builder.addFormDataPart("video",
                request.getFileName()!=null?request.getFileName():"video.mp4",
                RequestBody.create(request.getFileData(), MediaType.parse("video/mp4"))
                );
        addMultipartCaption(builder, request);
        addMultipartOptions(builder, request);

        RequestBody body = builder.build();
        Request httpRequest = new Request.Builder()
                .url(apiUrl + "/sendVideo")
                .post(body)
                .build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private TelegramMessageResponse sendVideoByUrl(TelegramMessageRequest request) throws TelegramApiException, JsonProcessingException {
        ObjectNode json=buildBasePayload(request);
        json.put("video",request.getFileUrl());
        addCaptionIfPresent(json,request);
        String payload=objectMapper.writeValueAsString(json);
        RequestBody body= RequestBody.create(payload,JSON_MEDIA_TYPE);
        Request httpRequest=new Request.Builder()
                .url(apiUrl+"/sendVideo")
                .post(body)
                .build();
        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    private void addCaptionIfPresent(ObjectNode json,TelegramMessageRequest request){
        if(request.getCaption()!=null){
            String caption=truncateText(request.getCaption(), MAX_CAPTION_LENGTH,"Caption");
            json.put("caption",caption);
            if (request.getParseMode() != null &&
                    request.getParseMode() != TelegramParseMode.NONE) {
                json.put("caption_parse_mode", request.getParseMode().getValue());
            }
        }
    }

    private void addMultipartCaption(MultipartBody.Builder multipartBuilder, TelegramMessageRequest request) {
        if(request.getCaption()!=null){
            String caption=truncateText(request.getCaption(), MAX_CAPTION_LENGTH,"Caption");
            multipartBuilder.addFormDataPart("caption",caption);
            if (request.getParseMode() != null &&
                    request.getParseMode() != TelegramParseMode.NONE) {
                multipartBuilder.addFormDataPart("caption_parse_mode", request.getParseMode().getValue());
            }
        }
    }
    private void addMultipartOptions(MultipartBody.Builder builder,
                                     TelegramMessageRequest request) {
        if (request.getDisableNotification() != null) {
            builder.addFormDataPart(
                    "disable_notification",
                    request.getDisableNotification().toString()
            );
        }

        if (request.getReplyToMessageId() != null) {
            builder.addFormDataPart(
                    "reply_to_message_id",
                    request.getReplyToMessageId().toString()
            );
        }
    }
}
