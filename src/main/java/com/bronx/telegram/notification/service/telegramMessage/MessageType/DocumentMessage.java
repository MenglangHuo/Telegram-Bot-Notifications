package com.bronx.telegram.notification.service.telegramMessage.MessageType;

import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import com.bronx.telegram.notification.service.telegramMessage.AbstractTelegramMessageStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class DocumentMessage extends AbstractTelegramMessageStrategy {
    public DocumentMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl, telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try {
            request.validate();
            if (request.getFileUrl() == null) {
                return this.sendDocumentByUpload(request);
            } else {
                return this.sendDocumentByUrl(request);
            }
        } catch (RuntimeException e) {
            throw new TelegramApiException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.DOCUMENT;
    }
    private TelegramMessageResponse sendDocumentByUrl(TelegramMessageRequest request)
            throws IOException, TelegramApiException {
        ObjectNode json = buildBasePayload(request);
        json.put("document", request.getFileUrl());

        if (request.getCaption() != null) {
            String caption = truncateText(request.getCaption(), MAX_CAPTION_LENGTH, "Caption");
            json.put("caption", caption);
        }

        String payload = objectMapper.writeValueAsString(json);
        RequestBody body = RequestBody.create(
                payload,
                MediaType.get("application/json; charset=utf-8")
        );

        Request httpRequest = new Request.Builder()
                .url(apiUrl + "/sendDocument")
                .post(body)
                .build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        }
    }

    private TelegramMessageResponse sendDocumentByUpload(TelegramMessageRequest request)
            throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        builder.addFormDataPart("chat_id", request.getChatId());

        String mimeType = request.getMimeType() != null ?
                request.getMimeType() : "application/octet-stream";

        builder.addFormDataPart(
                "document",
                request.getFileName() != null ? request.getFileName() : "document.pdf",
                RequestBody.create(request.getFileData(), MediaType.parse(mimeType))
        );

        if (request.getCaption() != null) {
            builder.addFormDataPart("caption", request.getCaption());
        }

        RequestBody body = builder.build();

        Request httpRequest = new Request.Builder()
                .url(apiUrl + "/sendDocument")
                .post(body)
                .build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        }
    }
}
