package com.bronx.notification.dto.subscription;
import com.bronx.notification.dto.organizationUnit.OrganizationMainResponse;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.model.enumz.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionResponse(
        Long id,
        String name,
        OrganizationMainResponse organization,
        SubscriptionPlanResponse plan,
        Long remainingCredits,
        SubscriptionStatus status,
        Instant startDate,
        Instant endDate,
        boolean isValid
) {}
