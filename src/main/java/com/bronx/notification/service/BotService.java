package com.bronx.notification.service;
import com.bronx.notification.dto.telegrambot.BotRequest;
import com.bronx.notification.dto.telegrambot.TelegramBotResponse;

public interface BotService {
    TelegramBotResponse createNewBot(BotRequest request);
}
