package com.bronx.telegram.notification.dto.telegrambot;

import com.bronx.telegram.notification.dto.partner.PartnerMainResponse;
import com.bronx.telegram.notification.dto.subscription.SubscriptionMainResponse;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelegramBotResponse(
        Long id,
        String botToken,
        String botUsername,
        String botName,
        String webhookUrl,
        BotStatus status,
        JsonNode botConfig,
        PartnerMainResponse partner,
        SubscriptionMainResponse subscription,
        TelegramBot botInstance,
        Instant createdAt,
        Instant updateAt,
        String createdBy,
        String updatedBy

        ) {
}
