package com.bronx.telegram.notification.dto.organizationUnit;

import com.bronx.telegram.notification.model.enumz.UnitType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrganizationMainResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long parentId;
    private String parentName;
    private String unitName;
    private String unitCode;
    private UnitType unitType;
}
