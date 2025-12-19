package com.bronx.telegram.notification.dto.division;

public record DivisionMainResponse(
        Long id,
        String divisionName,
        String divisionCode,
        String managerName,
        String managerEmail
) {
}
