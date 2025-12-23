package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.telegrambot.BotRequest;
import com.bronx.telegram.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.mapper.TelegramBotMapper;
import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.repository.SubscriptionRepository;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.service.BotService;
import com.bronx.telegram.notification.service.SubscriptionValidateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final TelegramBotRepository telegramBotRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionValidateService validationService;
    private final TelegramBotServiceImpl telegramBotService;
    private final TelegramBotMapper telegramBotMapper;

    @Value("${telegram.webhook.base-url}")
    private String baseUrl;

    @Value("${telegram.webhook.path}")
    private String path;

    @Override
    @Transactional
    public TelegramBotResponse createNewBot(BotRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));
        if (!validationService.canCreateBot(subscription)) {
            throw new BusinessException("Cannot create bot: subscription limit reached or subscription not active");
        }
        TelegramBot telegramBot = TelegramBot.builder()
                .subscription(subscription)
                .partner(subscription.getPartner())
                .botName(request.botName())
                .botUsername(request.botUsername())
                .botToken(request.botToken())
                .status(BotStatus.ACTIVE)
                .webhookVerified(false)
                .build();
        try {
            TelegramBot savedBot = telegramBotRepository.save(telegramBot);
            String webhookUrl = String.format("%s%s/%s", baseUrl, path, savedBot.getId());
            savedBot.setWebhookUrl(webhookUrl);
            savedBot = telegramBotRepository.save(savedBot);

            telegramBotService.registerBot(savedBot);

            log.info("Created bot {} for subscription {} (scope: {})",
                    savedBot.getBotUsername(),
                    subscription.getId(),
                    subscription.getScope() != null ? subscription.getScope().getUnitName() : "Company");
            return telegramBotMapper.toResponse(savedBot);
        } catch (RuntimeException e) {
            log.error("Telegram bot creation failed", e);
            throw new BusinessException("Telegram bot creation failed: " + e.getMessage());
        }

    }

    @Override
    public TelegramBot getBotForScope(Long subscriptionId, String scopeLevel) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));
        return telegramBotRepository
                .findFirstBySubscriptionIdAndStatus(subscriptionId, BotStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("No active bot found for subscription"));
    }
}
