package com.bronx.notification.service.impl.creditMessage;

import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.dto.creditUsage.PendingCreditUsage;
import com.bronx.notification.model.entity.CreditUsage;
import com.bronx.notification.repository.CreditUsageRepository;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.SubscriptionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scheduled job that syncs pending credit usage from Redis to the database.
 * Runs every N seconds (configurable) and processes records in batches.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreditSyncScheduler {
    private final PendingCreditRepository pendingCreditRepository;
    private final CreditUsageRepository creditUsageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final CreditConfig creditConfig;

    @Value("${server.port:8080}")
    private String serverPort;
    private String instanceId;
    @PostConstruct
    public void init() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String shortId = UUID.randomUUID().toString().substring(0, 6);
            this.instanceId = String.format("%s:%s-%s", hostname, serverPort, shortId);
        } catch (Exception e) {
            this.instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
        }
        log.info("🚀 CreditSyncScheduler initialized on instance: {}", instanceId);
    }

    /**
     * Sync pending credit usage to database.
     * Runs at fixed rate based on configuration.
     *
     * Uses @SchedulerLock to ensure only ONE instance executes this at a time:
     * - lockAtMostFor: Maximum time the lock should be held (prevents deadlock if
     * instance crashes)
     * - lockAtLeastFor: Minimum time to hold lock (prevents rapid re-execution)
     */
    @Scheduled(fixedRateString = "${credit.sync.interval:20000}")
    @SchedulerLock(name = "creditSyncScheduler", lockAtMostFor = "PT4M", // Max 4 minutes (should complete well before)
            lockAtLeastFor = "PT3S" // Min 3 seconds (prevents rapid re-execution)
    )
    @Transactional
    public void syncPendingCredits() {
        log.info("Pending credit Scheduler....");
        if (!creditConfig.isRedisBatchMode() || !creditConfig.getSync().isEnabled()) {
            return;
        }
        long pendingCount = pendingCreditRepository.getPendingCount();
        log.info("Pending cound: {}.... at: {}",pendingCount,Instant.now());
        if (pendingCount == 0) {
            return;
        }

        log.info("🔒 [{}] Acquired lock, starting credit sync with {} pending records",
                instanceId, pendingCount);

        Instant startTime = Instant.now();
        int batchSize = creditConfig.getSync().getBatchSize();
        int totalProcessed = 0;
        int retryCount = 0;
        int maxRetries = creditConfig.getSync().getRetryCount();
        log.info("retry count count, {} maRetry {}",retryCount, maxRetries);
        while (pendingCount > 0 && retryCount < maxRetries) {

            try {
                int processed = processBatch(batchSize);
                totalProcessed += processed;
                log.info("processed, {} batchSize records {}", processed,batchSize);
                if (processed < batchSize) {
                    break; // No more records
                }
                pendingCount = pendingCreditRepository.getPendingCount();
                log.info("pending count, {}", pendingCount);
            } catch (Exception e) {
                retryCount++;
                log.error("Error processing credit sync batch (retry {}/{}): {}",
                        retryCount, maxRetries, e.getMessage());
                if (retryCount >= maxRetries) {
                    log.error("Max retries exceeded for credit sync", e);
                    break;
                }
            }
        }
        if (totalProcessed > 0) {
            long duration = java.time.Duration.between(startTime, Instant.now()).toMillis();
            log.info("✅ [{}] Credit sync completed: {} records in {}ms",
                    instanceId, totalProcessed, duration);
        }
    }
    /**
     * Process a batch of pending credit usage records
     */
    private int processBatch(int batchSize) {
        List<PendingCreditUsage> batch = pendingCreditRepository.getPendingBatch(batchSize);
        log.info("batch size: {}",batch.size());
        if (batch.isEmpty()) {
            return 0;
        }
        // Group by subscription for credit deduction
        Map<Long, Long> creditsBySubscription = batch.stream()
                .collect(Collectors.groupingBy(
                        PendingCreditUsage::getSubscriptionId,
                        Collectors.summingLong(PendingCreditUsage::getUsedCredits)));

        log.info("batch subscriptions: {}",creditsBySubscription.toString());
        // Update subscription credits
        for (Map.Entry<Long, Long> entry : creditsBySubscription.entrySet()) {
            subscriptionRepository.updateCredits(entry.getKey(), entry.getValue());
        }

        // Create credit usage records
        String batchId = "batch-" + UUID.randomUUID().toString().substring(0, 8);

        List<CreditUsage> usageRecords = new ArrayList<>();
        for (PendingCreditUsage pending : batch) {
            CreditUsage usage = CreditUsage.builder()
                    .usedCredits(pending.getUsedCredits())
                    .usedAt(pending.getUsedAt())
                    .description(pending.getDescription())
                    .batchId(batchId)
                    .build();

            // Set subscription reference
            subscriptionRepository.findById(pending.getSubscriptionId())
                    .ifPresent(usage::setSubscription);


            // Set notification reference if available
            if (pending.getNotificationId() != null) {
                notificationRepository.findById(pending.getNotificationId())
                        .ifPresent(usage::setNotification);
            }
            usageRecords.add(usage);
        }
        // Batch save all usage records
        creditUsageRepository.saveAll(usageRecords);
        // Remove processed records from Redis
        List<String> trackingIds = batch.stream()
                .map(PendingCreditUsage::getTrackingId)
                .toList();
        pendingCreditRepository.removePendingBatch(trackingIds);
        log.debug("Processed batch of {} credit usage records (batchId: {})", batch.size(), batchId);
        return batch.size();
    }
    /**
     * Force sync all pending credits (for shutdown/maintenance)
     */
    @Transactional
    public void forceSyncAll() {
        log.info("Force syncing all pending credits...");
        long pendingCount = pendingCreditRepository.getPendingCount();
        int processed = 0;
        while (pendingCount > 0) {
            int batchProcessed = processBatch(100);
            processed += batchProcessed;
            if (batchProcessed == 0)
                break;
            pendingCount = pendingCreditRepository.getPendingCount();
        }
        log.info("Force sync completed: {} records processed", processed);
    }
}
