package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.telegrambot.BotRequest;
import com.bronx.telegram.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.telegram.notification.model.entity.TelegramBot;

public interface BotService {
    TelegramBotResponse createNewBot(BotRequest request);
    TelegramBot getBotForScope(Long subscriptionId, String scopeLevel);
}
