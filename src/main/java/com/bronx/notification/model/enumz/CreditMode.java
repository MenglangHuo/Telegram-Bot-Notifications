package com.bronx.notification.model.enumz;

/**
 * Credit processing mode enumeration.
 *
 * REDIS_BATCH: Fast mode using Redis for credit operations with batch DB sync
 * DIRECT_DB: Direct database operations (slower but strongly consistent)
 */
public enum CreditMode {
    /**
     * Uses Redis for atomic credit check/decrement with eventual consistency.
     * Credit usage records are batched and synced to DB periodically.
     * Best for high-throughput scenarios (1000+ messages/second).
     */
    REDIS_BATCH,

    /**
     * Direct database operations for each credit usage.
     * Provides strong consistency but lower throughput.
     * Best for low-volume scenarios or when strong consistency is required.
     */
    DIRECT_DB
}

