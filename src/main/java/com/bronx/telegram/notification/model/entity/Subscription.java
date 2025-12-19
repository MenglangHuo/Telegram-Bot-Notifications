package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.beans.Transient;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends SoftDeletableAuditable<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrganizationUnit scope;

    @Column(name = "subscription_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    @Column(name = "subscription_name",length = 50)
    private String subscriptionName;

    @Column(name = "max_telegram_bots")
    private Integer maxTelegramBots = 1;

    @Column(name = "max_telegram_channels")
    private Integer maxTelegramChannels = 5;

    @Column(name = "max_employees")
    private Integer maxEmployees = 100;

    @Column(name = "max_notifications_per_month")
    private Integer maxNotificationsPerMonth = 10000;

    @Column(name = "status",length = 30)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "last_reset_date")
    private Instant lastResetDate;


    @Column(name = "features", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode features; // e.g., {"priority_support": true, "analytics": true}


    @Column(name = "current_notifications_count")
    private Integer currentNotificationsCount = 0;

    @Column(name = "current_bot_count")
    private Integer currentBotCount = 0;

    @Transient
    public boolean isValid() {
        if (status != SubscriptionStatus.ACTIVE) return false;
        if (endDate != null && Instant.now().isAfter(endDate)) return false;
        return true;
    }

    @Transient
    public boolean canSendNotification() {
        return isValid() &&
                (maxNotificationsPerMonth == null ||
                        currentNotificationsCount < maxNotificationsPerMonth);
    }

    @Transient
    public boolean canAddBot() {
        return isValid() &&
                (maxTelegramBots == null || currentBotCount < maxTelegramBots);
    }

    @Transient
    public int getRemainingNotifications() {
        if (maxNotificationsPerMonth == null) return Integer.MAX_VALUE;
        return Math.max(0, maxNotificationsPerMonth - currentNotificationsCount);
    }

    public void incrementNotificationCount() {
        this.currentNotificationsCount =
                (this.currentNotificationsCount == null ? 0 : this.currentNotificationsCount) + 1;
    }

    public void resetMonthlyCounters() {
        this.currentNotificationsCount = 0;
        this.lastResetDate = Instant.now();
    }



}
