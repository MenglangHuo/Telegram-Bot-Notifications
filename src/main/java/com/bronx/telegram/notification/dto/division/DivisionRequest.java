package com.bronx.telegram.notification.dto.division;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionRequest {
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Division name is required")
    @Size(max = 100)
    private String divisionName;

    @NotBlank(message = "Division code is required")
    @Size(max = 50)

    private String divisionCode;

    @NotBlank(message = "Manager name is required")
    @Size(max = 50)
    private String managerName;

    @Email(message = "Invalid Email")
    @Size(max = 70)
    private String managerEmail;
}
