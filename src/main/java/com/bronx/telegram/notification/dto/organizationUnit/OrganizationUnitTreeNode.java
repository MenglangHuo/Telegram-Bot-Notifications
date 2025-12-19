package com.bronx.telegram.notification.dto.organizationUnit;

import com.bronx.telegram.notification.model.enumz.UnitStatus;
import com.bronx.telegram.notification.model.enumz.UnitType;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUnitTreeNode {
    private Long id;
    private String unitName;
    private String unitCode;
    private UnitType unitType;
    private UnitStatus status;
    private Integer employeeCount;
    private Integer hierarchyLevel;
    private Boolean isLeaf;
    private Boolean hasChildren;
    private List<OrganizationUnitTreeNode> children;
}
