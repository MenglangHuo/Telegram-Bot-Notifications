package com.bronx.notification.dto.subscriptionHistory;

import com.bronx.notification.model.enumz.SubscriptionAction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class SubscriptionHistoryRequest {
    private Long subscriptionId;
    private Long planId;
    private Instant startDate;
    private Instant endDate;
    private Long grantedCredits;
    private Long usedCredits;
    private SubscriptionAction action;
}
