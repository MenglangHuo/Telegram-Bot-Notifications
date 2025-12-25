package com.bronx.notification.service.impl.telegramMessage.MessageType;

import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.service.impl.TelegramBotClient;
import com.bronx.notification.service.impl.telegramMessage.AbstractTelegramMessageStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class DocumentMessage extends AbstractTelegramMessageStrategy {

    private static final String PARSE_MODE_HTML = "HTML"; // Define the parse mode

    public DocumentMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper) {
        super(apiUrl, telegramBotClient, objectMapper);
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try {
//            request.validate();
            if (request.getUrl() == null) {
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
        json.put("document", request.getUrl());

        if (request.getMessage() != null) {
            String caption = truncateText(request.getMessage(), MAX_CAPTION_LENGTH, "Caption");
            json.put("caption", caption);
            json.put("parse_mode",PARSE_MODE_HTML);
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

        String mimeType = "application/octet-stream";

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
