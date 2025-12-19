package com.bronx.telegram.notification.dto.organizationUnit;

import com.bronx.telegram.notification.model.enumz.UnitStatus;
import com.bronx.telegram.notification.model.enumz.UnitType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUnitResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long parentId;
    private String parentName;
    private String unitName;
    private String unitCode;
    private UnitType unitType;
    private String path;
    private Integer hierarchyLevel;
    private String fullPath;
    private UnitStatus status;
    private JsonNode metadata;
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    private Boolean isLeaf;
    private Integer employeeCount;
    private Integer childrenCount;
    private Integer displayOrder;
    private Instant createdAt;
    private Instant updatedAt;

    // Helper fields
    private List<OrganizationUnitResponse> children;
    private List<String> breadcrumbs; // ["Company", "Division", "Department"]
}

