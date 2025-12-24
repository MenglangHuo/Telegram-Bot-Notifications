package com.bronx.notification.utils;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MediaDownloader {

        private final OkHttpClient httpClient;

        public byte[] download(String url) {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new RuntimeException("Failed to download audio");
                }
                return response.body().bytes();
            } catch (IOException e) {
                throw new RuntimeException("Audio download error", e);
            }
        }


}
