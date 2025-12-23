package com.bronx.telegram.notification.service.telegramMessage;

import com.bronx.telegram.notification.dto.telegram.TelegramMessageRequest;
import com.bronx.telegram.notification.dto.telegram.TelegramMessageResponse;
import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramMessageStrategy {
    TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException;
    TelegramMessageType getMessageType();
    boolean supports(TelegramMessageType messageType);
}
