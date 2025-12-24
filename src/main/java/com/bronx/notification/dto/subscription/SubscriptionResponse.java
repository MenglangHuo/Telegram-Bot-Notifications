package com.bronx.notification.dto.subscription;

import com.bronx.notification.dto.organizationUnit.OrganizationMainResponse;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.model.enumz.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        Long orgUnitId,
        String orgUnitName,
        SubscriptionPlanResponse plan,
        Long remainingCredits,
        SubscriptionStatus status,
        Instant startDate,
        Instant endDate,
        boolean isValid
) {}
