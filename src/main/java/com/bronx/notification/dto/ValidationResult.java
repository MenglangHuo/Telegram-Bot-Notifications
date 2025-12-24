package com.bronx.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 5. Validation Result Helper
@Getter
@AllArgsConstructor
public class ValidationResult {
    private final boolean success;
    private final String message;

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }
}
