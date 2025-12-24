package com.bronx.notification.dto.subscriptionPlan;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(
        Long id,
        String code,
        String name,
        Integer durationMonths,
        Long notificationsCredit,
        boolean isUnlimitedDuration,
        BigDecimal price
) {
}
