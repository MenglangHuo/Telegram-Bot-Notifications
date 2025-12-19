package com.bronx.telegram.notification.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EmployeeRequest(
        @NotNull(message = "Company is required")
        Long companyId,
        @NotNull(message = "Organization is required")
        Long organizationUnitId,
        @NotEmpty(message = "Employee Code is required")
        String employeeCode,
        String managerCode,
        @NotEmpty(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        @NotEmpty(message = "FullName is required")
        String fullName,
        String role,
        String contact,
        String telegramUsername,
        Boolean isManager,
        Boolean isHeadOfDepartment,
        Boolean isHeadOfDivision
) {
    public EmployeeRequest {
        // Validation in constructor

        if (companyId == null) {
            throw new IllegalArgumentException("organizationId is required");
        }
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeCode is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
    }
}
