package com.bronx.telegram.notification.dto.department;


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
public class DepartmentRequest {
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long divisionId; // Optional

    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String departmentName;

    @NotBlank(message = "Department code is required")
    @Size(max = 50)

    private String departmentCode;

    @NotBlank(message = "Head name is required")
    @Size(max = 60)
    private String headName;

    @Email(message = "Invalid Email!")
    @Size(max = 70)
    private String headEmail;
}
