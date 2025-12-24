package com.bronx.telegram.notification.dto.employee;

import com.bronx.telegram.notification.dto.partner.PartnerMainResponse;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        String telegramBotDeepLink,
        Instant registeredAt,
        Boolean isManager,
        Boolean isHeadOfDivision,
        PartnerMainResponse partner
) {
}
