package com.bronx.notification.dto.subscriptionPlan;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubscriptionPlanRequest(
        @NotEmpty(message = "Code must not be empty")
        String code,
        @NotEmpty(message = "Name must not be empty")
        String name,
        @NotNull
        Integer durationMonths,
        @NotNull
        Long notificationsCredit,
        boolean
        isUnlimitedDuration,
        @NotNull
        BigDecimal price
) {}
