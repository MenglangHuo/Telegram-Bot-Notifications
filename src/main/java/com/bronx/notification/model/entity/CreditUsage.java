package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.Auditable;
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
@Table(name = "credit_usage")
public class CreditUsage extends Auditable<Long> {

    @ManyToOne
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    private short usedCredits;

    private Instant usedAt;
}

