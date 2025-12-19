package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.ChannelStatus;
import com.bronx.telegram.notification.model.enumz.ChartType;
import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "telegram_channels",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"bot_id", "chat_id"})
        })
public class TelegramChannel extends SoftDeletableAuditable<Long> implements Serializable {

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    private TelegramBot bot;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "chat_type", nullable = false,length = 20)
    @Enumerated(EnumType.STRING)
    private ChartType chatType;

    @Column(name = "channel_status", nullable = false,length = 20)
    @Enumerated(EnumType.STRING)
    private ChannelStatus channelStatus;

    @Column(name = "chat_name",length = 200, nullable = false)
    private String chatName;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    // Scope - which level does this channel serve?
//    @Column(name = "scope_type", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private ChannelScope scopeType;
//
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;


    @Column(name = "is_bot_admin")
    private Boolean isBotAdmin = false;

    @Column(name = "can_pin_messages")
    private Boolean canPinMessages = false;

    // ✅ ENHANCEMENT: Notification settings for this channel
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_settings", columnDefinition = "jsonb")
    private JsonNode notificationSettings; // e.g., {"disable_notification": false, "auto_pin": true}

    @Column(name = "member_count")
    private Integer memberCount;




    @PrePersist
    @PreUpdate
    private void validateChannelScope() {
        // Channel must belong to same subscription scope
        if (subscription.getSubscriptionType() == SubscriptionType.ORGANIZATION) {
            organization = subscription.getOrganization();
            division = null;
            department = null;
        } else if (subscription.getSubscriptionType() == SubscriptionType.DIVISION) {
            division = subscription.getDivision();
            organization = division.getOrganization();
            department = null;
        } else if (subscription.getSubscriptionType() == SubscriptionType.DEPARTMENT) {
            department = subscription.getDepartment();
            organization = department.getOrganization();
            division = department.getDivision();
        }
    }

    @Transient
    public String getScopeLevel() {
        if (department != null) return "DEPARTMENT";
        if (division != null) return "DIVISION";
        if (organization != null) return "ORGANIZATION";
        return "UNKNOWN";
    }
}
