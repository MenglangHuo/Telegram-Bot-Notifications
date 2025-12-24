package com.bronx.telegram.notification.dto.subscription;

import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionMainResponse(
        Long id,
        String subscriptionName,
        SubscriptionType subscriptionType,
        Integer maxTelegramBots,
        Integer maxTelegramChannels,
        Integer maxEmployees,
        Integer maxNotificationsPerMonth,
        Instant startDate,
        Instant endDate
) {
}
