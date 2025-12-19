package com.bronx.telegram.notification.dto.department;

public record DepartmentMainResponse(
        Long id,
        String departmentName,
        String departmentCode,
        String headName,
        String headEmail
) {
}
