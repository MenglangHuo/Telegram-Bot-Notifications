package com.bronx.notification.service;


import com.bronx.notification.dto.telegrambot.BotRequest;
import com.bronx.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.notification.model.entity.TelegramBot;

public interface BotService {
    TelegramBotResponse createNewBot(BotRequest request);
    TelegramBot getBotForScope(Long subscriptionId, String scopeLevel);
}
