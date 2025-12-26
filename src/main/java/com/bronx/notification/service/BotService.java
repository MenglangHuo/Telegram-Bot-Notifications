package com.bronx.notification.service;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.dto.telegrambot.BotRequest;
import com.bronx.notification.dto.telegrambot.TelegramBotResponse;
import org.hibernate.query.Page;

public interface BotService {
    TelegramBotResponse createNewBot(BotRequest request);
//    Page<TelegramBotResponse> listSubscriptionPlans(Pageable pageable);
}
