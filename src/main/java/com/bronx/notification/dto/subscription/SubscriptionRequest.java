package com.bronx.notification.dto.subscription;

import com.bronx.notification.model.enumz.SubscriptionAction;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
public record SubscriptionRequest(
        @NotNull
        Long orgUnitId,
        @NotNull
        Long planId,
        String name,
        SubscriptionAction subscriptionAction,
        Instant startDate,
        Instant endDate
) {}