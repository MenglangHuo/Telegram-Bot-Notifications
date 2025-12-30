package com.bronx.notification.service.impl.creditMessage;


import com.bronx.notification.dto.creditUsage.CreditOperationResult;

/**
 * Credit service interface for managing subscription credits.
 * Implementations can use Redis (fast, eventual consistency) or
 * direct database (slower, strong consistency).
 */
public interface CreditService {
    /**
     * Check if subscription has sufficient credits
     *
     * @param subscriptionId the subscription ID
     * @param amount         credits required
     * @return true if sufficient credits available
     */
    boolean hasCredits(Long subscriptionId, long amount);
    /**
     * Get current available credits for a subscription
     *
     * @param subscriptionId the subscription ID
     * @return available credits, or -1 if subscription not found
     */
    long getCurrentCredits(Long subscriptionId);
    /**
     * Check and decrement credits atomically.
     * If insufficient credits, no decrement occurs.
     *
     * @param subscriptionId the subscription ID
     * @param amount         credits to deduct
     * @param notificationId optional notification ID for tracking
     * @return operation result with success status and remaining credits
     */
    CreditOperationResult checkAndDecrementCredit(Long subscriptionId, long amount, Long notificationId);
    /**
     * Initialize/refresh credits for a subscription.
     * Used when subscription is created or credits need to be synced.
     *
     * @param subscriptionId the subscription ID
     * @param credits        initial credit amount
     */
    void initializeCredits(Long subscriptionId, long credits);
    /**
     * Rollback a credit deduction (e.g., when message sending fails)
     *
     * @param subscriptionId the subscription ID
     * @param amount         credits to restore
     * @param trackingId     optional tracking ID from original operation
     */
    void rollbackCredit(Long subscriptionId, long amount, String trackingId);
}

