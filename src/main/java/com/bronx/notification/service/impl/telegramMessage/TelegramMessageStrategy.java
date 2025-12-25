package com.bronx.notification.service.impl.telegramMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.model.enumz.TelegramMessageType;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramMessageStrategy {
    TelegramMessageResponse send(TelegramMessageRequest request) throws TelegramApiException;
    TelegramMessageType getMessageType();
    boolean supports(TelegramMessageType messageType);
}
