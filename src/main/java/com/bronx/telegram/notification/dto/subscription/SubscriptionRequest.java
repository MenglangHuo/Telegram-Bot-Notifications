package com.bronx.telegram.notification.dto.subscription;

import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotNull(message = "Subscription type is required")
    private SubscriptionType subscriptionType;

    // Only ONE of these should be set based on subscriptionType
    private Long organizationId;
    private Long divisionId;
    private Long departmentId;

    @Size(max = 50)
    private String subscriptionName;

    @Max(value = 10, message = "Max bots must be at least 1")
    private Integer maxTelegramBots = 1;

    @Max(value = 50, message = "Max channels must be at least 1")
    private Integer maxTelegramChannels = 5;

    @Max(value = 500, message = "Max employees must be at least 1")
    private Integer maxEmployees = 100;

    @Max(value = 500000, message = "Max notifications must be at least 1")
    private Integer maxNotificationsPerMonth = 10000;

    @NotNull(message = "Start date is required")
    private Instant startDate;

    private Instant endDate;
}
