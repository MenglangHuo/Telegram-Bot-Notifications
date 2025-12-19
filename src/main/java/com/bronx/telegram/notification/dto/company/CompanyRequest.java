package com.bronx.telegram.notification.dto.company;

public record CompanyRequest(
        Long partnerId,
    String name,
    String code,
        String description) {
}
