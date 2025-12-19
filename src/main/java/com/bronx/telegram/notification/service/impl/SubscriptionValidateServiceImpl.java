package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.ValidationResult;
import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.repository.SubscriptionRepository;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.repository.TelegramChannelRepository;
import com.bronx.telegram.notification.service.SubscriptionValidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionValidateServiceImpl implements SubscriptionValidateService {

    private final SubscriptionRepository subscriptionRepository;
    private final TelegramBotRepository botRepository;
    private final TelegramChannelRepository channelRepository;


    @Override
    public boolean canCreateBot(Subscription subscription) {
        if (!subscription.isValid()) {
            log.info("Subscription validation failed. Subscription: {}", subscription);
            return false;
        }

        Integer currentBots = botRepository.countActiveBySubscriptionId(subscription.getId());
        return currentBots < subscription.getMaxTelegramBots();
    }

    @Override
    public boolean canAddChannel(Subscription subscription) {
        if (!subscription.isValid()) {
            return false;
        }

        Integer currentChannels = channelRepository.countActiveBySubscriptionId(subscription.getId());
        return currentChannels < subscription.getMaxTelegramChannels();
    }

    @Override
    public ValidationResult validateNotificationLimit(Subscription subscription) {
        if (!subscription.isValid()) {
            return ValidationResult.failure("Subscription is not active or has expired");
        }

//        if (!subscription.canSendNotification()) {
//            return ValidationResult.failure(
//                    String.format("Monthly notification limit reached: %d/%d",
//                            subscription.getNotificationsSentThisMonth(),
//                            subscription.getMaxNotificationsPerMonth())
//            );
//        }

        return ValidationResult.success();
    }

    @Override
    @Transactional
    public void resetMonthlyCounters() {
        Instant now = Instant.now();
        Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);

        List<Subscription> subscriptions = subscriptionRepository
                .findAllByLastResetDateBefore(oneMonthAgo);

        for (Subscription sub : subscriptions) {
//            sub.setNotificationsSentThisMonth(0);
            sub.setLastResetDate(now);
        }

        subscriptionRepository.saveAll(subscriptions);
        log.info("Reset monthly counters for {} subscriptions", subscriptions.size());
    }
}
