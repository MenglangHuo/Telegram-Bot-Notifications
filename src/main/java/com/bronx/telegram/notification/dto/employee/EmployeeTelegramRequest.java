package com.bronx.telegram.notification.dto.employee;

public record EmployeeTelegramRequest(
        String employeeCode,
        String email,
        String contact,
        String fullName,
        String telegramUserId,
        String telegramUsername,
        String telegramChatId,
        String role
        ) {
}
