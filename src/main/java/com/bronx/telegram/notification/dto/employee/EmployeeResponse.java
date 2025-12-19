package com.bronx.telegram.notification.dto.employee;

import com.bronx.telegram.notification.dto.partner.PartnerMainResponse;
import com.bronx.telegram.notification.model.enumz.BotStatus;

import java.time.Instant;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String managerCode,
        String email,
        String fullName,
        String role,
        String telegramUserId,
        String telegramUsername,
        String telegramChatId,
        BotStatus status,
        Instant registeredAt,
        Boolean isManager,
        Boolean isHeadOfDivision,
        PartnerMainResponse partner
) {
}
