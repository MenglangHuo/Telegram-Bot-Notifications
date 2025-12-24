package com.bronx.notification.service;


import com.bronx.notification.model.entity.TelegramBot;

public interface TelegramBotService {
    void initializeBots();
    void registerBot(TelegramBot bot);
    void unregisterBot(Long botId);
    TelegramBot findBotForCompany(Long companyId);
//    TelegramBotClient getBotClient(Long botId);
    void shutdown();

    void refreshBot(Long botId);

}
