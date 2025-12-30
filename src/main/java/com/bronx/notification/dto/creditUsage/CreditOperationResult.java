package com.bronx.notification.dto.creditUsage;

import lombok.*;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CreditOperationResult {
    private boolean success;
    private long remainingCredits;
    private String message;
    private String trackingId;
    public static CreditOperationResult success(long remainingCredits, String trackingId) {
        return CreditOperationResult.builder()
                .success(true)
                .remainingCredits(remainingCredits)
                .trackingId(trackingId)
                .build();
    }
    public static CreditOperationResult insufficientCredits(long currentCredits) {
        return CreditOperationResult.builder()
                .success(false)
                .remainingCredits(currentCredits)
                .message("Insufficient credits")
                .build();
    }
    public static CreditOperationResult failure(String message) {
        return CreditOperationResult.builder()
                .success(false)
                .remainingCredits(-1)
                .message(message)
                .build();
    }
}
