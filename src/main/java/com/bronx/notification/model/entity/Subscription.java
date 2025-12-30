package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import com.bronx.notification.model.enumz.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.beans.Transient;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends SoftDeletableAuditable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrganizationUnit scope;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(length=100)
    private String name;

    @Column(name = "remaining_credits")
    private Long remainingCredits;

    @Column(name = "status",length = 30)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "limit_durations")
    @Builder.Default
    private Boolean limitDurations=false;


    @Transient
    public boolean isValid() {
        if (status == SubscriptionStatus.CANCELLED ) return false;
        if (endDate != null && Instant.now().isAfter(endDate)) return false;
        return true;
    }

    @Transient
    public boolean isDeleted(){
        return this.getDeletedAt()!=null;
    }

    @Transient
    public boolean hasCredits(long amount) {
        return remainingCredits != null && remainingCredits >= amount;
    }
}
