package com.bronx.telegram.notification.dto.telegrambot;

public record BotRequest(
        String botToken,
        String botUsername,
        String botName,
        Long subscriptionId
) {
}
