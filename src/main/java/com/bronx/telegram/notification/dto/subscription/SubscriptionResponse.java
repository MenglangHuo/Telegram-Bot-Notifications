package com.bronx.telegram.notification.dto.subscription;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationMainResponse;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponse {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private SubscriptionType subscriptionType;
    private String subscriptionName;
    private Integer maxTelegramBots;
    private Integer maxTelegramChannels;
    private Integer maxEmployees;
    private Integer maxNotificationsPerMonth;
    private SubscriptionStatus status;
    private Instant startDate;
    private Instant endDate;
    private OrganizationMainResponse scope;

    // Usage statistics
    private Integer currentBotCount;
    private Integer currentChannelCount;
    private Integer currentEmployeeCount;
    private Integer notificationsSentThisMonth;

    //audit
    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
}
