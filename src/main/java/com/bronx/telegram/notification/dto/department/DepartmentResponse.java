package com.bronx.telegram.notification.dto.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long divisionId;
    private String divisionName;
    private String departmentName;
    private String departmentCode;
    private String headName;
    private String headEmail;

    // Statistics
    private Integer employeeCount;
    private Integer activeSubscriptionCount;

    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
}
