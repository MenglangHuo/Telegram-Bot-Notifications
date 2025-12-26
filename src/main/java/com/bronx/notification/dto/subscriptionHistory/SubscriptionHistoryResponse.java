package com.bronx.notification.dto.subscriptionHistory;

import com.bronx.notification.dto.subscription.SubscriptionMainResponse;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.model.enumz.SubscriptionAction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Builder
public class SubscriptionHistoryResponse {
    private Long id;
    private SubscriptionMainResponse subscription;
    private SubscriptionPlanResponse plan;
    private Instant startDate;
    private Instant endDate;
    private Long grantedCredits;
    private Long usedCredits;
    private SubscriptionAction action;
}
