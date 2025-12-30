package com.bronx.notification.dto.creditUsage;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class PendingCreditUsage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long subscriptionId;
    private Long notificationId;
    private short usedCredits;
    private Instant usedAt;
    private String description;
    /**
     * UUID for tracking this pending usage
     */
    private String trackingId;
}

