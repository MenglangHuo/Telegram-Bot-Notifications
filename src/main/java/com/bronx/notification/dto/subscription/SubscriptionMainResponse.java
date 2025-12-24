package com.bronx.notification.dto.subscription;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionMainResponse(
        Long id,
        String subscriptionName,
        Integer maxTelegramBots,
        Integer maxTelegramChannels,
        Integer maxEmployees,
        Integer maxNotificationsPerMonth,
        Instant startDate,
        Instant endDate
) {
}
