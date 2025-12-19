package com.bronx.telegram.notification.dto.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    @NotBlank(message = "Organization name is required")
    @Size(max = 100)
    private String organizationName;

    @NotBlank(message = "Organization code is required")
    @Size(max = 70)

    private String organizationCode;

    private Boolean hierarchyEnabled = true;
}
