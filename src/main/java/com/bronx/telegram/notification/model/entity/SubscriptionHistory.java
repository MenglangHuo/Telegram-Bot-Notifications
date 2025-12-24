package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


public class SubscriptionHistory {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;


}
