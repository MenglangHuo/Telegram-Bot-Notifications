package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import com.bronx.notification.model.enumz.SubscriptionAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_history")
public class SubscriptionHistory extends SoftDeletableAuditable<Long> {

    @ManyToOne
    private Subscription subscription;

    @ManyToOne
    private SubscriptionPlan plan;

    private Instant startDate;
    private Instant endDate;

    private Long grantedCredits;
    private Long usedCredits;

    @Enumerated(EnumType.STRING)
    private SubscriptionAction action;
    // NEW, RENEW, UPGRADE, DOWNGRADE
}

