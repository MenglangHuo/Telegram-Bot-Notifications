package com.bronx.notification.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TelegramBotClient {
    private final String botToken;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.get("application/json; charset=utf-8");
    private static final int MAX_MESSAGE_LENGTH = 4096;

    public TelegramBotClient(String botToken) {
        this.botToken = botToken;
        this.apiUrl = "https://api.telegram.org/bot" + botToken;
        this.objectMapper = new ObjectMapper();
        // Enhanced HTTP client with connection pooling
        ConnectionPool connectionPool = new ConnectionPool(
                10,                    // Max idle connections
                5, TimeUnit.MINUTES    // Keep-alive duration
        );

        this.httpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    requestCount.incrementAndGet();

                    // Add custom headers
                    request = request.newBuilder()
                            .addHeader("Connection", "keep-alive")
                            .build();

                    return chain.proceed(request);
                })
                .build();
    }
    public int getRequestCount() {
        return requestCount.get();
    }

    public void resetRequestCount() {
        requestCount.set(0);
    }

    public JsonNode verifyToken() {
        try {
            Request request = new Request.Builder()
                    .url(apiUrl + "/getMe")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("❌ Empty response from Telegram");
                    return null;
                }

                String body = response.body().string();
                JsonNode json = objectMapper.readTree(body);

                if (json.get("ok").asBoolean(false)) {
                    JsonNode result = json.get("result");
                    String username = result.get("username").asText();
                    String firstName = result.get("first_name").asText();
                    log.info("✅ Bot verified: @{} ({})", username, firstName);
                    return result;
                } else {
                    String error = json.has("description") ?
                            json.get("description").asText() : "Unknown";
                    log.error("❌ Bot verification failed: {}", error);

                    // Common errors
                    if (error.contains("token")) {
                        log.error("💡 Solution: Check bot token is correct");
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to verify bot token", e);
        }
        return null;
    }

    public boolean setWebhook(String webhookUrl, List<String> allowedUpdates) {
        try {
            // Validate webhook URL
            if (!webhookUrl.startsWith("https://")) {
                log.error("❌ Webhook URL must use HTTPS: {}", webhookUrl);
                return false;
            }

            ObjectNode json = objectMapper.createObjectNode();
            json.put("url", webhookUrl);
            json.put("max_connections", 100);
            json.put("drop_pending_updates", false);

            if (allowedUpdates != null && !allowedUpdates.isEmpty()) {
                ArrayNode updates = objectMapper.createArrayNode();
                allowedUpdates.forEach(updates::add);
                json.set("allowed_updates", updates);
            }

            String payload = objectMapper.writeValueAsString(json);
            RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);

            Request request = new Request.Builder()
                    .url(apiUrl + "/setWebhook")
                    .post(body)
                    .build();

            log.info("🔗 Setting webhook: {}", webhookUrl);
            log.info("📋 Allowed updates: {}", allowedUpdates);

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("❌ Empty response from Telegram");
                    return false;
                }

                String responseBody = response.body().string();
                JsonNode result = objectMapper.readTree(responseBody);
                boolean ok = result.get("ok").asBoolean(false);

                if (ok) {
                    log.info("✅ Webhook set successfully: {}", webhookUrl);
                    return true;
                } else {
                    String error = result.has("description") ?
                            result.get("description").asText() : "Unknown error";
                    log.error("❌ Failed to set webhook: {}", error);

                    // Common errors and solutions
                    if (error.contains("HTTPS")) {
                        log.error("💡 Solution: Use HTTPS with valid SSL certificate");
                    } else if (error.contains("getaddrinfo") || error.contains("resolve")) {
                        log.error("💡 Solution: Domain not accessible from internet");
                    } else if (error.contains("Connection refused")) {
                        log.error("💡 Solution: Server not accepting connections");
                    }

                    log.error("📄 Full error response: {}", responseBody);
                }
            }
        } catch (Exception e) {
            log.error("❌ Exception setting webhook", e);
        }
        return false;
    }

    public JsonNode getWebhookInfo() {
        try {
            Request request = new Request.Builder()
                    .url(apiUrl + "/getWebhookInfo")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("❌ Empty response");
                    return null;
                }

                String body = response.body().string();
                JsonNode json = objectMapper.readTree(body);

                if (json.get("ok").asBoolean(false)) {
                    JsonNode result = json.get("result");

                    log.info("=== Webhook Info ===");
                    log.info("URL: {}",
                            result.has("url") ? result.get("url").asText() : "NOT SET");
                    log.info("Pending updates: {}",
                            result.has("pending_update_count") ?
                                    result.get("pending_update_count").asInt() : 0);

                    if (result.has("last_error_date")) {
                        long errorDate = result.get("last_error_date").asLong();
                        log.warn("⚠️ Last error: {} (timestamp: {})",
                                result.has("last_error_message") ?
                                        result.get("last_error_message").asText() : "Unknown",
                                errorDate);
                    }

                    return result;
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to get webhook info", e);
        }
        return null;
    }

    public String sendMessageWithReturn(String chatId, String text, Boolean silent,
                                        String parseMode, Integer replyToMessageId) {
        try {
            // Validate inputs
            if (chatId == null || chatId.isEmpty()) {
                log.error("❌ Chat ID is null or empty");
                return null;
            }

            if (text == null || text.trim().isEmpty()) {
                log.error("❌ Message text is null or empty");
                return null;
            }

            // Truncate if needed
            String originalText = text;
            if (text.length() > MAX_MESSAGE_LENGTH) {
                log.warn("⚠️ Message truncated from {} to {} chars",
                        text.length(), MAX_MESSAGE_LENGTH);
                text = text.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
            }

            // Build request
            ObjectNode json = objectMapper.createObjectNode();
            json.put("chat_id", chatId);
            json.put("text", text);

            if (parseMode != null && !parseMode.isEmpty()) {
                json.put("parse_mode", parseMode);
            }

            if (Boolean.TRUE.equals(silent)) {
                json.put("disable_notification", true);
            }

            if (replyToMessageId != null) {
                json.put("reply_to_message_id", replyToMessageId);
            }

            String payload = objectMapper.writeValueAsString(json);
            RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);

            log.debug("📤 Sending to chat_id: {} ({}), length: {} chars, parseMode: {}",
                    chatId,
                    chatId.startsWith("-") ? "group/channel" : "private",
                    text.length(),
                    parseMode);

            Request request = new Request.Builder()
                    .url(apiUrl + "/sendMessage")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("❌ Response body is null (status: {})", response.code());
                    return null;
                }

                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JsonNode result = objectMapper.readTree(responseBody);

                    if (result.get("ok").asBoolean(false)) {
                        String messageId = result.get("result").get("message_id").asText();
                        log.info("✅ Message sent successfully to chat {} (msg_id: {})",
                                chatId, messageId);
                        return messageId;
                    } else {
                        String error = result.has("description") ?
                                result.get("description").asText() : "Unknown error";
                        Integer errorCode = result.has("error_code") ?
                                result.get("error_code").asInt() : null;

                        log.error("❌ Telegram API error [{}]: {}", errorCode, error);

                        // Handle specific errors
                        handleTelegramError(errorCode, error, chatId);

                        log.error("📄 Full response: {}", responseBody);
                    }
                } else {
                    log.error("❌ HTTP error {}: {}", response.code(), response.message());
                    log.error("📄 Response: {}", responseBody);

                    // Handle HTTP errors
                    if (response.code() == 401) {
                        log.error("💡 Solution: Bot token is invalid");
                    } else if (response.code() == 429) {
                        log.error("💡 Solution: Rate limit exceeded, slow down");
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("❌ JSON parsing error: {}", e.getMessage());
        } catch (IOException e) {
            log.error("❌ Network error sending message: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending message to chat {}", chatId, e);
        }
        return null;
    }

    private void handleTelegramError(Integer errorCode, String error, String chatId) {
        if (errorCode == null) return;

        switch (errorCode) {
            case 400:
                if (error.contains("chat not found")) {
                    log.error("💡 Solution: Chat ID {} is invalid or bot was blocked", chatId);
                } else if (error.contains("message is too long")) {
                    log.error("💡 Solution: Message exceeds 4096 characters");
                } else if (error.contains("parse")) {
                    log.error("💡 Solution: Invalid HTML/Markdown formatting");
                }
                break;
            case 403:
                log.error("💡 Solution: Bot was blocked by user or removed from chat");
                break;
            case 429:
                log.error("💡 Solution: Too many requests, implement rate limiting");
                break;
            default:
                log.error("💡 Check Telegram Bot API documentation for error code {}",
                        errorCode);
        }
    }

    public boolean pinMessage(String chatId, String messageId) {
        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("chat_id", chatId);
            json.put("message_id", messageId);
            json.put("disable_notification", false);

            String payload = objectMapper.writeValueAsString(json);
            RequestBody body = RequestBody.create(payload, JSON_MEDIA_TYPE);

            Request request = new Request.Builder()
                    .url(apiUrl + "/pinChatMessage")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    return false;
                }

                String responseBody = response.body().string();
                JsonNode result = objectMapper.readTree(responseBody);
                boolean success = result.get("ok").asBoolean(false);

                if (success) {
                    log.info("📌 Message {} pinned in chat {}", messageId, chatId);
                } else {
                    String error = result.has("description") ?
                            result.get("description").asText() : "Unknown";
                    log.error("❌ Failed to pin message: {}", error);
                }

                return success;
            }
        } catch (Exception e) {
            log.error("❌ Error pinning message {} in chat {}", messageId, chatId, e);
        }
        return false;
    }

    public void shutdown() {
        try {
            log.info("🛑 Shutting down TelegramBotClient (processed {} requests)",
                    requestCount.get());

            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();

            if (!httpClient.dispatcher().executorService()
                    .awaitTermination(5, TimeUnit.SECONDS)) {
                httpClient.dispatcher().executorService().shutdownNow();
            }

            log.info("✅ TelegramBotClient shutdown completed");
        } catch (InterruptedException e) {
            log.error("❌ Error during shutdown", e);
            httpClient.dispatcher().executorService().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String sendMessageWithKeyboard(String chatId, String text, String parseMode, String keyboardJson) {
        try {
            // 1. Basic Validation
            if (chatId == null || chatId.isEmpty() || text == null || text.trim().isEmpty()) {
                log.error("❌ Chat ID or Text is null/empty");
                return null;
            }

            // 2. Truncate text if it exceeds Telegram limits
            if (text.length() > MAX_MESSAGE_LENGTH) {
                text = text.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
            }

            // 3. Build the primary JSON payload
            ObjectNode payloadJson = objectMapper.createObjectNode();
            payloadJson.put("chat_id", chatId);
            payloadJson.put("text", text);

            if (parseMode != null && !parseMode.isEmpty()) {
                payloadJson.put("parse_mode", parseMode);
            }

            // 4. Parse the keyboardJson string into a JsonNode and add it as 'reply_markup'
            if (keyboardJson != null && !keyboardJson.isEmpty()) {
                try {
                    JsonNode keyboardNode = objectMapper.readTree(keyboardJson);
                    payloadJson.set("reply_markup", keyboardNode);
                } catch (JsonProcessingException e) {
                    log.error("❌ Invalid Keyboard JSON provided: {}", keyboardJson);
                    // Continue without keyboard or return null based on your preference
                }
            }

            String payloadString = objectMapper.writeValueAsString(payloadJson);
            RequestBody body = RequestBody.create(payloadString, JSON_MEDIA_TYPE);

            // 5. Build and execute the HTTP Request
            Request request = new Request.Builder()
                    .url(apiUrl + "/sendMessage")
                    .post(body)
                    .build();

            log.debug("📤 Sending keyboard message to chat_id: {}", chatId);

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    log.error("❌ Response body is null (status: {})", response.code());
                    return null;
                }

                String responseBody = response.body().string();
                JsonNode result = objectMapper.readTree(responseBody);

                if (response.isSuccessful() && result.get("ok").asBoolean(false)) {
                    String messageId = result.get("result").get("message_id").asText();
                    log.info("✅ Keyboard message sent. ID: {}", messageId);
                    return messageId;
                } else {
                    String error = result.has("description") ? result.get("description").asText() : "Unknown";
                    int errorCode = result.has("error_code") ? result.get("error_code").asInt() : response.code();

                    log.error("❌ Telegram API error [{}]: {}", errorCode, error);
                    handleTelegramError(errorCode, error, chatId);
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("❌ Network error sending keyboard message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error in sendMessageWithKeyboard", e);
        }
        return null;
    }

    public String getApiUrl() {
        return apiUrl;
    }
    public OkHttpClient getHttpClient() {
        return httpClient;
    }
    public String getBotToken() {
        return botToken;
    }

}