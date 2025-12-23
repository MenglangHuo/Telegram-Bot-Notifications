package com.bronx.telegram.notification.service.telegramMessage.MessageType;

import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import com.bronx.telegram.notification.service.telegramMessage.AbstractTelegramMessageStrategy;
import com.bronx.telegram.notification.utils.MediaDownloader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class AudioMessage extends AbstractTelegramMessageStrategy {
    private final MediaDownloader mediaDownloader;
    private final String THUMBNAIL_URL="/images/thumb.png";

    public AudioMessage(String apiUrl, TelegramBotClient telegramBotClient, ObjectMapper objectMapper, MediaDownloader mediaDownloader) {
        super(apiUrl, telegramBotClient, objectMapper);
        this.mediaDownloader = mediaDownloader;
    }

    @Override
    public TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException {
        try {
            request.validate();


            // file data is already provided (direct upload)
            if (request.getFileData() != null) {
                if (request.getFileName() == null) {
                    request.setFileName("audio.mp3");
                }
                return sendAudioByUpload(request);
            }

            // file url download first
            if(request.getFileUrl()!=null && request.getTitle()!=null&&request.getPerformer()!=null){
                log.info("Downloading audio from URL to extract metadata...");
                byte[] audioData = mediaDownloader.download(request.getFileUrl());
                request.setFileData(audioData);
                if (request.getFileName() == null) {
                    request.setFileName("audio.mp3");
                }
                log.info("Audio downloaded, proceeding to send with metadata.");
                return sendAudioByUpload(request);

            }
            // fallback
            if (request.getFileUrl() != null) {
                return sendAudioByUrl(request);
            }
                return this.sendAudioByUpload(request);


        } catch (RuntimeException e) {
            throw new TelegramApiException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public TelegramMessageType getMessageType() {
        return TelegramMessageType.AUDIO;
    }

    private TelegramMessageResponse sendAudioByUrl(TelegramMessageRequest request)
            throws IOException, TelegramApiException {

        ObjectNode json = buildBasePayload(request);
        json.put("audio", request.getFileUrl());
        json.put("thumbnail", THUMBNAIL_URL);
        String payload = objectMapper.writeValueAsString(json);

        RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);

        Request httpRequest = new Request.Builder()
                .url(apiUrl + "/sendAudio")
                .post(body)

                .build();

        try (Response response =
                     telegramBotClient.getHttpClient()
                             .newCall(httpRequest)
                             .execute()) {

            return handleResponse(response, request);
        }
    }


    private TelegramMessageResponse sendAudioByUpload(TelegramMessageRequest request)
            throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        builder.addFormDataPart("chat_id", request.getChatId());
        builder.addFormDataPart(
                "audio",
                request.getFileName() != null ? request.getFileName() : "audio.mp3",
                RequestBody.create(request.getFileData(), MediaType.parse("audio/mpeg"))
        );

        if (request.getCaption() != null) {
            builder.addFormDataPart(
                    "caption",
                    truncateText(
                            request.getCaption(),
                            MAX_CAPTION_LENGTH,
                            "Caption"
                    )
            );
        }
        if (request.getCaption() != null) {
            builder.addFormDataPart(
                    "caption",
                    truncateText(
                            request.getCaption(),
                            MAX_CAPTION_LENGTH,
                            "Caption"
                    )
            );
        }
        byte[] thumbData = getThumbnailBytes();
        if (thumbData != null) {
            builder.addFormDataPart(
                    "thumbnail",
                    "thumb.jpg",
                    RequestBody.create(thumbData, MediaType.parse("images/jpeg"))
            );
        }
        if (request.getTitle() != null) {
            builder.addFormDataPart("title", request.getTitle());
        }

        if (request.getPerformer() != null) {
            builder.addFormDataPart("performer", request.getPerformer());
        }

        if (request.getDuration() != null) {
            builder.addFormDataPart(
                    "duration",
                    String.valueOf(request.getDuration())
            );
        }

        RequestBody body = builder.build();
        Request httpRequest = new Request.Builder()
                .url(apiUrl + "/sendAudio")
                .post(body)
                .build();

        try (Response response = telegramBotClient.getHttpClient().newCall(httpRequest).execute()) {
            return handleResponse(response, request);
        }
    }

    private byte[] getThumbnailBytes() {
        try (InputStream is = getClass().getResourceAsStream(THUMBNAIL_URL)) {
            if (is == null) {
                log.error("Thumbnail not found in resources: {}", THUMBNAIL_URL);
                return null;
            }
            return is.readAllBytes();
        } catch (IOException e) {
            log.error("Failed to read thumbnail from resources", e);
            return null;
        }
    }

}
