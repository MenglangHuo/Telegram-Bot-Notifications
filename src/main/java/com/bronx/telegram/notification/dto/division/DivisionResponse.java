package com.bronx.telegram.notification.dto.division;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String divisionName;
    private String divisionCode;
    private String managerName;
    private String managerEmail;


    // Statistics
    private Integer departmentCount;
    private Integer employeeCount;
    private Integer activeSubscriptionCount;

    //audit
    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
}
