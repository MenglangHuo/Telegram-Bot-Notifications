package com.bronx.telegram.notification.dto.organization;

import jakarta.validation.constraints.NotEmpty;

public record SecretRequest(
        @NotEmpty(message = "Secret Key Must not Be Empty ")
       String secretKey,
        @NotEmpty(message = "Credential Must not Be Empty ")
        String credential
) {
}
