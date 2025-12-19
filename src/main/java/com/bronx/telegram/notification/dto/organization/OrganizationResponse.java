package com.bronx.telegram.notification.dto.organization;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private String organizationName;
    private String organizationCode;
    private Boolean hierarchyEnabled;


    private Integer divisionCount;
    private Integer departmentCount;
    private Integer employeeCount;
    private Integer activeSubscriptionCount;


    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;

}
