package com.bronx.notification.service.impl.creditMessage.creditRedis;

import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.dto.creditUsage.CreditOperationResult;
import com.bronx.notification.dto.creditUsage.PendingCreditUsage;
import com.bronx.notification.repository.SubscriptionRepository;
import com.bronx.notification.service.impl.creditMessage.CreditService;
import com.bronx.notification.service.impl.creditMessage.PendingCreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
/**
 * Redis-based credit service with atomic operations using Lua scripts.
 * Provides sub-millisecond credit checks and decrements.
 */
@Service("redisCreditService")
@RequiredArgsConstructor
@Slf4j
public class RedisCreditService implements CreditService {
    private final RedisTemplate<String, Long> creditRedisTemplate;
//    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> creditDecrementScript;
    private final DefaultRedisScript<Long> creditCheckScript;
    private final CreditConfig creditConfig;
    private final SubscriptionRepository subscriptionRepository;
    private final PendingCreditRepository pendingCreditRepository;
    /**
     * Credit key TTL - 24 hours (will be refreshed on access)
     */
    private static final long CREDIT_KEY_TTL_HOURS = 24;
    @Override
    public boolean hasCredits(Long subscriptionId, long amount) {
        long currentCredits = getCurrentCredits(subscriptionId);
        return currentCredits >= amount;
    }
    @Override
    public long getCurrentCredits(Long subscriptionId) {
        String key = getCreditKey(subscriptionId);
        // Try to get from Redis first
        Long credits = creditRedisTemplate.execute(creditCheckScript,
                Collections.singletonList(key));
        // If not in Redis (-1), load from database
        if (credits == null || credits == -1) {
            credits = loadCreditsFromDatabase(subscriptionId);
            if (credits >= 0) {
                // Cache in Redis
                initializeCredits(subscriptionId, credits);
            }
        }
        return credits != null ? credits : -1;
    }
    @Override
    public CreditOperationResult checkAndDecrementCredit(Long subscriptionId, long amount, Long notificationId) {
        String key = getCreditKey(subscriptionId);
        // Ensure credits are in Redis
        ensureCreditsLoaded(subscriptionId);
        // Execute atomic Lua script
        Long result = creditRedisTemplate.execute(creditDecrementScript,
                Collections.singletonList(key),
                amount);
        if (result == null) {
            log.error("Redis script returned null for subscription {}", subscriptionId);
//            return CreditOperationResult.failure("Redis operation failed");
        }

        if (result == -1) {
            // Insufficient credits
            long currentCredits = getCurrentCredits(subscriptionId);
            log.warn("Insufficient credits for subscription {}: requested {}, available {}",
                    subscriptionId, amount, currentCredits);
            return CreditOperationResult.insufficientCredits(currentCredits);
        }
        // Success - create pending credit usage record
        String trackingId = UUID.randomUUID().toString();
        PendingCreditUsage pending = PendingCreditUsage.builder()
                .subscriptionId(subscriptionId)
                .notificationId(notificationId)
                .usedCredits((short) amount)
                .usedAt(Instant.now())
                .trackingId(trackingId)
                .description("Notification credit usage")
                .build();

        pendingCreditRepository.savePending(pending);
        log.debug("Credit decremented for subscription {}: {} credits used, {} remaining",
                subscriptionId, amount, result);
        return CreditOperationResult.success(result, trackingId);
    }

    @Override
    public void initializeCredits(Long subscriptionId, long credits) {
        String key = getCreditKey(subscriptionId);
        creditRedisTemplate.opsForValue().set(key, credits, CREDIT_KEY_TTL_HOURS, TimeUnit.HOURS);
        log.info("Initialized credits for subscription {}: {}", subscriptionId, credits);
    }
    @Override
    public void rollbackCredit(Long subscriptionId, long amount, String trackingId) {
        String key = getCreditKey(subscriptionId);
        // Atomic increment to restore credits
        Long newValue = creditRedisTemplate.opsForValue().increment(key, amount);
        // Remove from pending if tracking ID provided
        if (trackingId != null) {
            pendingCreditRepository.removePending(trackingId);
        }
        log.info("Rolled back {} credits for subscription {}, new balance: {}",
                amount, subscriptionId, newValue);
    }
    /**
     * Get Redis key for subscription credits
     */
    private String getCreditKey(Long subscriptionId) {

        return creditConfig.getRedis().getKeyPrefix() + subscriptionId;
    }
    /**
     * Ensure credits are loaded into Redis from database
     */
    private void ensureCreditsLoaded(Long subscriptionId) {
        String key = getCreditKey(subscriptionId);
        Boolean exists = creditRedisTemplate.hasKey(key);
        if (exists == null || !exists) {
            long credits = loadCreditsFromDatabase(subscriptionId);
            if (credits >= 0) {
                initializeCredits(subscriptionId, credits);
            }
        }
    }
    /**
     * Load credits from database
     */
    private long loadCreditsFromDatabase(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .filter(s -> !s.isDeleted() && s.isValid())
                .map(s -> s.getRemainingCredits() != null ? s.getRemainingCredits() : 0L)
                .orElse(-1L);
    }
}

