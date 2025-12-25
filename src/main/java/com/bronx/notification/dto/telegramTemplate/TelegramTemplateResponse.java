package com.bronx.notification.dto.telegramTemplate;

public record TelegramTemplateResponse(
        Long id,
        String name,
        String htmlContent
) {
}
