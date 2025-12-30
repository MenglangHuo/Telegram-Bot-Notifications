package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
@Entity
@Table(name = "credit_usage", indexes = {
        @Index(name = "idx_credit_usage_subscription", columnList = "subscription_id"),
        @Index(name = "idx_credit_usage_used_at", columnList = "used_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditUsage extends Auditable<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private Notification notification;
    @Column(name = "used_credits", nullable = false)
    private short usedCredits;
    @Column(name = "used_at", nullable = false)
    private Instant usedAt;
    @Column(name = "description", length = 500)
    private String description;
    /**
     * Batch ID for records created via batch sync
     */
    @Column(name = "batch_id", length = 50)
    private String batchId;
}