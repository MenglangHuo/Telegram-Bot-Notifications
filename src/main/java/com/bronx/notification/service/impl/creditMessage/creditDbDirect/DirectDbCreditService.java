package com.bronx.notification.service.impl.creditMessage.creditDbDirect;
import com.bronx.notification.dto.creditUsage.CreditOperationResult;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.model.entity.CreditUsage;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.repository.CreditUsageRepository;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.SubscriptionRepository;
import com.bronx.notification.service.impl.creditMessage.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
/**
 * Direct database credit service.
 * Provides strong consistency but lower throughput than Redis mode.
 */
@Service("directDbCreditService")
@RequiredArgsConstructor
@Slf4j
public class DirectDbCreditService implements CreditService {
    private final SubscriptionRepository subscriptionRepository;
    private final CreditUsageRepository creditUsageRepository;
    private final NotificationRepository notificationRepository;
    @Override
    public boolean hasCredits(Long subscriptionId, long amount) {
        return subscriptionRepository.findById(subscriptionId)
                .filter(s -> !s.isDeleted() && s.isValid())
                .map(s -> s.getRemainingCredits() != null && s.getRemainingCredits() >= amount)
                .orElse(false);
    }
    @Override
    public long getCurrentCredits(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .filter(s -> !s.isDeleted() && s.isValid())
                .map(s -> s.getRemainingCredits() != null ? s.getRemainingCredits() : 0L)
                .orElse(-1L);
    }
    @Override
    @Transactional
    public CreditOperationResult checkAndDecrementCredit(Long subscriptionId, long amount, Long notificationId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription "+ subscriptionId));
        if (subscription.isDeleted() || !subscription.isValid()) {
            return CreditOperationResult.failure("Subscription is not active");
        }
        long currentCredits = subscription.getRemainingCredits() != null ? subscription.getRemainingCredits() : 0;
        if (currentCredits < amount) {
            log.warn("Insufficient credits for subscription {}: requested {}, available {}",
                    subscriptionId, amount, currentCredits);
            return CreditOperationResult.insufficientCredits(currentCredits);
        }
        // Atomic decrement using database
        int updated = subscriptionRepository.decrementCredits(subscriptionId, amount);
        if (updated == 0) {
            // Concurrent update - reload and check
            subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription"+ subscriptionId));
            return CreditOperationResult.insufficientCredits(subscription.getRemainingCredits());
        }
        // Create credit usage record
        String trackingId = UUID.randomUUID().toString();
        CreditUsage usage = CreditUsage.builder()
                .subscription(subscription)
                .usedCredits((short) amount)
                .usedAt(Instant.now())
                .description("Notification credit usage")
                .batchId("direct-" + trackingId)
                .build();
        if (notificationId != null) {
            notificationRepository.findById(notificationId)
                    .ifPresent(usage::setNotification);
        }
        creditUsageRepository.save(usage);
        long remainingCredits = currentCredits - amount;
        log.debug("Credit decremented (DB) for subscription {}: {} credits used, {} remaining",
                subscriptionId, amount, remainingCredits);
        return CreditOperationResult.success(remainingCredits, trackingId);
    }
    @Override
    public void initializeCredits(Long subscriptionId, long credits) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription "+ subscriptionId));
        subscription.setRemainingCredits(credits);
        subscriptionRepository.save(subscription);
        log.info("Initialized credits (DB) for subscription {}: {}", subscriptionId, credits);
    }
    @Override
    @Transactional
    public void rollbackCredit(Long subscriptionId, long amount, String trackingId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription "+ subscriptionId));
        long currentCredits = subscription.getRemainingCredits() != null ? subscription.getRemainingCredits() : 0;
        subscription.setRemainingCredits(currentCredits + amount);
        subscriptionRepository.save(subscription);
        log.info("Rolled back {} credits (DB) for subscription {}, new balance: {}",
                amount, subscriptionId, currentCredits + amount);
    }
}
