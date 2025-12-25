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
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Slf4j
public class VideoMessage extends AbstractTelegramMessageStrategy {
    private static final String PARSE_MODE_HTML = "HTML";

    public VideoMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl, telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try{
//            request.validate();
            if(request.getUrl()==null){
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
        json.put("video",request.getUrl());
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
        log.info("message: {}",request.getMessage());
        if(request.getMessage()!=null){
            String caption=truncateText(request.getMessage(), MAX_CAPTION_LENGTH,"Caption");
            json.put("caption",caption);
            if (request.getParseMode() != null &&
                    request.getParseMode() != TelegramParseMode.NONE) {
                json.put("parse_mode", PARSE_MODE_HTML);
            }
        }
    }

    private void addMultipartCaption(MultipartBody.Builder multipartBuilder, TelegramMessageRequest request) {
        if(request.getMessage()!=null){
            String caption=truncateText(request.getMessage(), MAX_CAPTION_LENGTH,"Caption");
            multipartBuilder.addFormDataPart("caption",caption);
            if (request.getParseMode() != null &&
                    request.getParseMode() != TelegramParseMode.NONE) {
                multipartBuilder.addFormDataPart("parse_mode", request.getParseMode().getValue());
            }
        }
    }
    private void addMultipartOptions(MultipartBody.Builder builder,
                                     TelegramMessageRequest request) {


    }
}
