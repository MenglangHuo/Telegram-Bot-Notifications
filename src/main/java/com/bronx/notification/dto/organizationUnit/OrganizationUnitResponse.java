package com.bronx.notification.dto.organizationUnit;

import com.bronx.notification.model.enumz.UnitStatus;
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
    private Long parentId;
    private String parentName;
    private String name;
    private String code;
    private String path;
    private Integer hierarchyLevel;
    private String fullPath;
    private UnitStatus status;
    private JsonNode metadata;

    private Boolean isLeaf;
    private Integer displayOrder;
    private Instant createdAt;
    private Instant updatedAt;

    // Helper fields
    private List<OrganizationUnitResponse> children;
    private List<String> breadcrumbs; // ["Company", "Division", "Department"]
}

