package com.bronx.notification.dto.creditUsage;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreditUsageRequest {
    private Long subscriptionId;
    private Long usedCredits;
    private Long notificationId;
    private Instant usageDate;
    private String description;
}
