package com.bronx.notification.dto.organizationUnit;

import com.bronx.notification.model.enumz.UnitStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationUnitTreeNode {
    private Long id;
    private String name;
    private String code;
    private UnitStatus status;
    private Integer employeeCount;
    private Integer hierarchyLevel;
    private Boolean isLeaf;
    private Boolean hasChildren;
    private List<OrganizationUnitTreeNode> children;
}
