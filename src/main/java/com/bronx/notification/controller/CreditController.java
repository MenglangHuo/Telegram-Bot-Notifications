package com.bronx.notification.controller;

import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.service.impl.creditMessage.CreditRecoveryService;
import com.bronx.notification.service.impl.creditMessage.CreditServiceFactory;
import com.bronx.notification.service.impl.creditMessage.PendingCreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/**
 * Admin controller for credit system management.
 */
@RestController
@RequestMapping("/api/v1/admin/credits")
@RequiredArgsConstructor
@Slf4j
public class CreditController {
    private final CreditConfig creditConfig;
    private final CreditServiceFactory creditServiceFactory;
    private final CreditRecoveryService creditRecoveryService;
    private final PendingCreditRepository pendingCreditRepository;
    /**
     * Get current credit system status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "mode", creditConfig.getMode(),
                "redisEnabled", creditConfig.getRedis().isEnabled(),
                "syncEnabled", creditConfig.getSync().isEnabled(),
                "syncInterval", creditConfig.getSync().getInterval(),
                "batchSize", creditConfig.getSync().getBatchSize(),
                "pendingCount", pendingCreditRepository.getPendingCount()));
    }
    /**
     * Get credits for a subscription
     */
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<Map<String, Object>> getSubscriptionCredits(
            @PathVariable Long subscriptionId) {
        long credits = creditServiceFactory.getCreditService().getCurrentCredits(subscriptionId);
        return ResponseEntity.ok(Map.of(
                "subscriptionId", subscriptionId,
                "remainingCredits", credits,
                "mode", creditConfig.getMode()));
    }
    /**
     * Manually refresh subscription credits from database to Redis
     */
    @PostMapping("/subscription/{subscriptionId}/refresh")
    public ResponseEntity<Map<String, String>> refreshSubscriptionCredits(
            @PathVariable Long subscriptionId) {
        if (creditConfig.isRedisBatchMode()) {
            creditRecoveryService.refreshSubscriptionCredits(subscriptionId);
            return ResponseEntity.ok(Map.of(
                    "message", "Credits refreshed from database to Redis"));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "Credit refresh not needed in DIRECT_DB mode"));
        }
    }
    /**
     * Force sync all pending credits to database
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> forceSync() {
        long pendingBefore = pendingCreditRepository.getPendingCount();
        if (creditConfig.isRedisBatchMode() && pendingBefore > 0) {
            creditRecoveryService.onShutdown(); // Reuse the sync logic
            long pendingAfter = pendingCreditRepository.getPendingCount();
            return ResponseEntity.ok(Map.of(
                    "message", "Force sync completed",
                    "pendingBefore", pendingBefore,
                    "pendingAfter", pendingAfter,
                    "synced", pendingBefore - pendingAfter));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No pending records to sync or not in Redis batch mode",
                    "pendingCount", pendingBefore));
        }
    }
}
