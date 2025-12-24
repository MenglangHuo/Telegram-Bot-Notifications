package com.bronx.telegram.notification.dto.partner;

import com.bronx.telegram.notification.model.enumz.SubscriptionTier;
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
public class PartnerResponse {
    private Long id;
    private String partnerName;
    private String partnerCode;
    private String clientId;
    private String secretKey;
    private SubscriptionTier subscriptionTier;
    private Integer maxOrganizations;
    private Integer maxBots;
    private String contactName;
    private String contactEmail;
    private String contact;

    // Statistics
    private Integer currentOrganizationCount;
    private Integer currentBotCount;
    private Integer totalSubscriptions;

    //audit
    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
}
