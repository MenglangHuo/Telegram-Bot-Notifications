package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlan extends SoftDeletableAuditable<Long> implements Serializable {

    @Column(length = 100, nullable = false, unique = true)
    private String code; // BASIC_1M, PRO_6M

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    private Integer durationMonths; // 1,3,6,12

    @Column(name = "notification_credit", nullable = false, unique = true)
    private Long notificationsCredit;

    private boolean isUnlimitedDuration; //base on credit usage

    private BigDecimal price;
}
