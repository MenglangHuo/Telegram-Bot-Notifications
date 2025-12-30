package com.bronx.notification.service.impl.creditMessage;


import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.dto.creditUsage.PendingCreditUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Repository for managing pending credit usage records in Redis.
 * These are synced to the database periodically by the CreditSyncScheduler.
 */
@Repository
@RequiredArgsConstructor
@Slf4j

public class PendingCreditRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CreditConfig creditConfig;
    private static final String PENDING_SET_KEY = "credit:pending:all";
    /**
     * Save a pending credit usage record
     */
    public void savePending(PendingCreditUsage pending) {
        String key = getPendingKey(pending.getTrackingId());
        // Store the pending record
        redisTemplate.opsForValue().set(key, pending);
        // Add to the set for batch retrieval
        redisTemplate.opsForSet().add(PENDING_SET_KEY, pending.getTrackingId());
        log.debug("Saved pending credit usage: {}", pending.getTrackingId());
    }
    /**
     * Get all pending credit usage records
     */
    public List<PendingCreditUsage> getAllPending() {
        Set<Object> trackingIds = redisTemplate.opsForSet().members(PENDING_SET_KEY);
        if (trackingIds == null || trackingIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<PendingCreditUsage> pendingList = new ArrayList<>();
        for (Object trackingId : trackingIds) {
            String key = getPendingKey(trackingId.toString());
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof PendingCreditUsage) {
                pendingList.add((PendingCreditUsage) value);
            }
        }
        return pendingList;
    }
    /**
     * Get pending records in batches
     */
    public List<PendingCreditUsage> getPendingBatch(int batchSize) {
        Set<Object> trackingIds = redisTemplate.opsForSet().members(PENDING_SET_KEY);
        log.info("tracking id: {}",trackingIds.toString());
        if (trackingIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<PendingCreditUsage> batch = new ArrayList<>();
        int count = 0;
        for (Object trackingId : trackingIds) {
            if (count >= batchSize)
                break;
            String key = getPendingKey(trackingId.toString());
            log.info("tracking key: {}",key);
            Object value = redisTemplate.opsForValue().get(key);
            log.info("tracking value: {}",value.toString());

            try {
                // ✅ Use ObjectMapper to convert
                PendingCreditUsage usage = objectMapper.convertValue(
                        value,
                        PendingCreditUsage.class
                );

                batch.add(usage);
                log.info("Successfully converted item {}", count);
                count++;
            } catch (Exception e) {
                log.error("Failed to convert value for key {}: {}", key, e.getMessage());
            }
        }
        return batch;
    }
    /**
     * Remove a pending credit usage record
     */
    public void removePending(String trackingId) {
        String key = getPendingKey(trackingId);
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(PENDING_SET_KEY, trackingId);
        log.debug("Removed pending credit usage: {}", trackingId);
    }
    /**
     * Remove multiple pending records (used after batch sync)
     */
    public void removePendingBatch(List<String> trackingIds) {
        if (trackingIds == null || trackingIds.isEmpty())
            return;
        List<String> keys = trackingIds.stream()
                .map(this::getPendingKey)
                .toList();
        log.info("batch key remove: {}",keys);
        redisTemplate.delete(keys);
        redisTemplate.opsForSet().remove(PENDING_SET_KEY, trackingIds.toArray());
        log.debug("Removed {} pending credit usage records", trackingIds.size());
    }
    /**
     * Get count of pending records
     */
    public long getPendingCount() {
        Long count = redisTemplate.opsForSet().size(PENDING_SET_KEY);
        return count != null ? count : 0;
    }
    private String getPendingKey(String trackingId) {
        return creditConfig.getRedis().getPendingKeyPrefix() + trackingId;
    }
}

