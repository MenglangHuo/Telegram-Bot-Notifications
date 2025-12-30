package com.bronx.notification.configs;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Configuration for distributed scheduled task locking using ShedLock.
 * Ensures only ONE instance runs scheduled tasks at a time in a multi-instance
 * deployment.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S") // 30 seconds max lock time
public class SchedulerConfig {
    /**
     * Redis-based lock provider for ShedLock.
     * Uses the existing Redis connection for distributed locking.
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "scheduler-lock");
    }
}
