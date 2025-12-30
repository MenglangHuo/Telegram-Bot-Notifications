package com.bronx.notification.configs;

import com.bronx.notification.model.enumz.CreditMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "credit")
@Data
public class CreditConfig {

    /**
     * Credit processing mode: REDIS_BATCH or DIRECT_DB
     */
    private CreditMode mode = CreditMode.REDIS_BATCH;

    private RedisSettings redis = new RedisSettings();
    private SyncSettings sync = new SyncSettings();

    @Data
    public static class RedisSettings {
        /**
         * Enable Redis credit caching
         */
        private boolean enabled = true;

        /**
         * Redis key prefix for subscription credits
         * e.g., "credit:subscription:123" = 5000 (remaining credits)
         */
        private String keyPrefix = "credit:subscription:";

        /**
         * Redis key prefix for pending credit usage records
         */
        private String pendingKeyPrefix = "credit:pending:";
    }

    @Data
    public static class SyncSettings {
        /**
         * Sync interval in milliseconds
         */
        private long interval = 5000;

        /**
         * Maximum records per batch insert
         */
        private int batchSize = 100;

        /**
         * Number of retries on sync failure
         */
        private int retryCount = 3;

        /**
         * Enable/disable the sync scheduler
         */
        private boolean enabled = true;
    }

    /**
     * Check if Redis batch mode is active
     */
    public boolean isRedisBatchMode() {
        return mode == CreditMode.REDIS_BATCH && redis.isEnabled();
    }
}