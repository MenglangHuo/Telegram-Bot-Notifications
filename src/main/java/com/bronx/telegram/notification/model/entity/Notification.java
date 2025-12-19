package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationPriority;
import com.bronx.telegram.notification.model.enumz.NotificationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.Instant;


@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Notification extends SoftDeletableAuditable<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "telegram_bot_id", nullable = false)
//    private TelegramBot telegramBot; // Which bot will send this notification

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "location",length = 100)
    private String location;

    @Column(name = "method",length = 50)
    private String method; //QR , FACE_ID, ...

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb")
    private JsonNode content; // Additional structured data

    @Column(name = "notification_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationEventType notificationType; // CHECK_IN, ANNOUNCEMENT, ALERT, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @Column(name = "channels", length = 100)
    private String channels = "TELEGRAM";

    // Retry tracking
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    // Delivery tracking
    @Column(name = "queued_at", nullable = false)
    private Instant queuedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "processing_at")
    private Instant processingAt;

    @Column(name = "sent_at" )
    private Instant sentAt;

    @Column(name = "processing_durations")
    private Long processingDurations;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "telegram_message_id")
    private String telegramMessageId; // Telegram's message ID after sending

    @Column(name = "scheduled_at")
    private Instant scheduledAt; // For scheduled notifications

    @Transient
    public boolean canRetry() {
        return retryCount < maxRetries &&
                status != NotificationStatus.SENT &&
                status != NotificationStatus.DELIVERED;
    }
}
