package com.bronx.telegram.notification.dto.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookMessage implements Serializable {
    private Long webhookId;
    private Long botId;
    private Long updateId;
    private String chatId;
    private String userId;
    private String username;
    private String messageType;
    private String command;
    private JsonNode content;
}
