package com.bronx.notification.dto.company;

public record CompanyRequest(
        Long partnerId,
    String name,
    String code,
        String description) {
}
