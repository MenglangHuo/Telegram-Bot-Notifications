package com.bronx.notification.service.impl.telegramMessage.MessageType;

import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.service.impl.TelegramBotClient;
import com.bronx.notification.service.impl.telegramMessage.AbstractTelegramMessageStrategy;
import com.bronx.notification.utils.MediaDownloader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Slf4j
public class AudioMessage extends AbstractTelegramMessageStrategy {
    private final MediaDownloader mediaDownloader;
    private static final String THUMBNAIL_URL = "/images/audio.png";
    private static final String PARSE_MODE_HTML = "HTML"; // Define the parse mode

    public AudioMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper, MediaDownloader mediaDownloader) {
        super(apiUrl, telegramBotClient, objectMapper);
        this.mediaDownloader = mediaDownloader;
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try {
            if (request.getFileData() != null) {
                if (request.getFileName() == null) request.setFileName("audio.mp3");
                return sendAudioByUpload(request);
            }

            if (request.getUrl() != null && request.getTitle() != null && request.getPerformer() != null) {
                log.info("Downloading audio from URL to extract metadata...");
                byte[] audioData = mediaDownloader.download(request.getUrl());
                request.setFileData(audioData);
                if (request.getFileName() == null) request.setFileName("audio.mp3");
                return sendAudioByUpload(request);
            }

            if (request.getUrl() != null) {
                return sendAudioByUrl(request);
            }

            return this.sendAudioByUpload(request);

        } catch (IOException e) {
            throw new TelegramApiException("IO Error: " + e.getMessage());
        } catch (Exception e) {
            throw new TelegramApiException(e.getMessage());
        }
    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.AUDIO;
    }

    private TelegramMessageResponse sendAudioByUrl(TelegramMessageRequest request) throws IOException, TelegramApiException {
        ObjectNode json = buildBasePayload(request);
        json.put("audio", request.getUrl());
        if(request.getAudioIconUrl()!=null)
            json.put("thumbnail", request.getAudioIconUrl());
        else json.put("thumbnail",THUMBNAIL_URL );

        // --- HTML Caption Support ---
        if (request.getCaption() != null) {
            json.put("caption", truncateText(request.getCaption(), MAX_CAPTION_LENGTH, "Caption"));
            json.put("parse_mode", PARSE_MODE_HTML);
        }

        String payload = objectMapper.writeValueAsString(json);
        RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);
        Request httpRequest = new Request.Builder().url(apiUrl + "/sendAudio").post(body).build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        }
    }

    private TelegramMessageResponse sendAudioByUpload(TelegramMessageRequest request) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        builder.addFormDataPart("chat_id", request.getChatId());
        builder.addFormDataPart(
                "audio",
                request.getFileName() != null ? request.getFileName() : "audio.mp3",
                RequestBody.create(request.getFileData(), MediaType.parse("audio/mpeg"))
        );

        // --- Enhanced HTML Caption Logic ---
        String caption = request.getCaption() != null ? request.getCaption() : request.getMessage();
        if (caption != null) {
            builder.addFormDataPart("caption", truncateText(caption, MAX_CAPTION_LENGTH, "Caption"));
            builder.addFormDataPart("parse_mode", PARSE_MODE_HTML);
        }

        byte[] thumbData = getThumbnailBytes(request.getAudioIconUrl());
        if (thumbData != null) {
            builder.addFormDataPart("thumbnail", "audio.jpg", RequestBody.create(thumbData, MediaType.parse("image/jpeg")));
        }

        if (request.getTitle() != null) builder.addFormDataPart("title", request.getTitle());
        if (request.getPerformer() != null) builder.addFormDataPart("performer", request.getPerformer());

        RequestBody body = builder.build();
        Request httpRequest = new Request.Builder().url(apiUrl + "/sendAudio").post(body).build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        }
    }

    private byte[] getThumbnailBytes(String iconUrl) {
        // Determine the source and open the stream inside try-with-resources
        try (InputStream is = (iconUrl != null && !iconUrl.isBlank())
                ? URI.create(iconUrl).toURL().openStream()
                : getClass().getResourceAsStream(THUMBNAIL_URL)) {

            if (is == null) {
                log.warn("Thumbnail source not found: {}", iconUrl != null ? iconUrl : THUMBNAIL_URL);
                return null;
            }

            return is.readAllBytes();
        } catch (IOException | IllegalArgumentException e) {
            log.error("Failed to read thumbnail. URL: {}, Error: {}", iconUrl, e.getMessage());
            return null;
        }
    }
}