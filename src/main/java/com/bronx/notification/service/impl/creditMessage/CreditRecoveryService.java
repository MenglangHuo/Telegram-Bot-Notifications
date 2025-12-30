package com.bronx.notification.service.impl.creditMessage;
import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.repository.SubscriptionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for recovering pending credits on application startup/shutdown.
 * Ensures no data loss when using Redis batch mode.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditRecoveryService {
    private final PendingCreditRepository pendingCreditRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RedisTemplate<String, Long> creditRedisTemplate;
    private final CreditConfig creditConfig;
    private final CreditSyncScheduler creditSyncScheduler;
    /**
     * On startup, check for any pending credit records and validate Redis state
     */
    @PostConstruct
    public void onStartup() {
        if (!creditConfig.isRedisBatchMode()) {
            log.info("Redis batch mode disabled, skipping credit recovery");
            return;
        }
        log.info("Starting credit recovery service...");
        try {
            // Check pending count
            long pendingCount = pendingCreditRepository.getPendingCount();
            if (pendingCount > 0) {
                log.warn("Found {} pending credit records from previous session, syncing...", pendingCount);
                creditSyncScheduler.forceSyncAll();
            }
            // Validate and sync Redis credits with database for active subscriptions
            syncRedisCreditsFromDatabase();
            log.info("✅ Credit recovery completed");
        } catch (Exception e) {
            log.error("Error during credit recovery: {}", e.getMessage(), e);
        }
    }
    /**
     * On shutdown, sync all pending credits to database
     */
    @PreDestroy
    public void onShutdown() {
        if (!creditConfig.isRedisBatchMode()) {
            return;
        }
        log.info("Shutting down, syncing pending credits...");
        try {
            long pendingCount = pendingCreditRepository.getPendingCount();
            if (pendingCount > 0) {
                creditSyncScheduler.forceSyncAll();
                log.info("Shutdown sync completed");
            }
        } catch (Exception e) {
            log.error("Error during shutdown sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Sync all active subscription credits from database to Redis
     */
    public void syncRedisCreditsFromDatabase() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findAllActive();
        for (Subscription subscription : activeSubscriptions) {
            if (subscription.getRemainingCredits() != null) {
                String key = creditConfig.getRedis().getKeyPrefix() + subscription.getId();
                // Only update if not already set (don't overwrite in-flight credits)
                Boolean exists = creditRedisTemplate.hasKey(key);
                if (exists == null || !exists) {
                    creditRedisTemplate.opsForValue().set(key, subscription.getRemainingCredits());
                    log.debug("Synced credits for subscription {}: {}",
                            subscription.getId(), subscription.getRemainingCredits());
                }
            }
        }
        log.info("Synced {} subscription credits to Redis", activeSubscriptions.size());
    }
    /**
     * Force refresh a specific subscription's credits from database
     */
    public void refreshSubscriptionCredits(Long subscriptionId) {
        subscriptionRepository.findById(subscriptionId)
                .filter(s -> !s.isDeleted() && s.isValid())
                .ifPresent(subscription -> {
                    String key = creditConfig.getRedis().getKeyPrefix() + subscriptionId;
                    creditRedisTemplate.opsForValue().set(key, subscription.getRemainingCredits());
                    log.info("Refreshed credits for subscription {}: {}",
                            subscriptionId, subscription.getRemainingCredits());
                });
    }
}
