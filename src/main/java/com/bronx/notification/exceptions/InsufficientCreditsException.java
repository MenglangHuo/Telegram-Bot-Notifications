package com.bronx.notification.exceptions;

public class InsufficientCreditsException extends BusinessException {
    private final Long subscriptionId;
    private final long requestedCredits;
    private final long availableCredits;
    public InsufficientCreditsException(Long subscriptionId, long requested, long available) {
        super(String.format("Insufficient credits for subscription %d: requested %d, available %d",
                subscriptionId, requested, available));
        this.subscriptionId = subscriptionId;
        this.requestedCredits = requested;
        this.availableCredits = available;
    }
    public Long getSubscriptionId() {
        return subscriptionId;
    }
    public long getRequestedCredits() {
        return requestedCredits;
    }
    public long getAvailableCredits() {
        return availableCredits;
    }
}

