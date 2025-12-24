package com.bronx.notification.dto.creditUsage;

import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.entity.Subscription;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class CreditUsageResponse {
    private Long id;
    private Subscription subscription;
    private Long usedCredits;
    private Notification notification;
    private Instant usageDate;
    private String description;
}
