package com.bronx.notification.dto.organizationUnit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationMainResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long parentId;
    private String parentName;
    private String unitName;
    private String unitCode;
}
