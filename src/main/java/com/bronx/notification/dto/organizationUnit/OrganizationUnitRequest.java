package com.bronx.notification.dto.organizationUnit;

import com.bronx.notification.model.enumz.UnitStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class OrganizationUnitRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Long parentId; // null for root units

    @NotBlank(message = "Unit name is required")
    @Size(max = 200, message = "Unit name must not exceed 200 characters")
    private String unitName;

    @NotBlank(message = "Unit code is required")
    @Size(max = 50, message = "Unit code must not exceed 50 characters")
    private String unitCode;

    private UnitStatus status = UnitStatus.ACTIVE;

    private JsonNode metadata;

    @Email(message = "Invalid manager email format")
    @Size(max = 100)
    private String managerEmail;

    @Size(max = 100)
    private String managerName;

    @Size(max = 30)
    private String managerPhone;

    private Boolean isLeaf = false;

    private Integer displayOrder = 0;
}