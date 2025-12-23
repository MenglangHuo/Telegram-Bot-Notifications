package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;

public interface TelegramBotService {
    void initializeBots();
    void registerBot(TelegramBot bot);
    void unregisterBot(Long botId);
    TelegramBot findBotForCompany(Long companyId);
    TelegramBotClient getBotClient(Long botId);
    void shutdown();
//    boolean isBotRegistered(Long botId);
    void refreshBot(Long botId);
    TelegramBot getBotForSubscription(Long subscriptionId);
    boolean sendPersonalMessage(
            Long botId,
            String chatId,
            String message,
            boolean urgent);
    public boolean sendChannelMessage(
            Long botId,
            String chatId,
            String message,
            boolean pinMessage,
            boolean disableNotification);
}
