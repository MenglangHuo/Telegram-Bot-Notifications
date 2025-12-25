package com.bronx.notification.service;

import com.bronx.notification.dto.telegramTemplate.TelegramTemplateRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;

public interface TelegramTemplateService {
    TelegramTemplateResponse createOrUpdate(TelegramTemplateRequest dto);
    TelegramTemplateResponse getByName(String name);
}
