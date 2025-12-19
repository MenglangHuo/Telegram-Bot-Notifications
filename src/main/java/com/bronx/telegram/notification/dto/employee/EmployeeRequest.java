package com.bronx.telegram.notification.dto.employee;

public record EmployeeRequest(
        Long organizationId,
        Long divisionId,
        Long departmentId,
        String employeeCode,
        String managerCode,
        String email,
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

        if (organizationId == null) {
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
