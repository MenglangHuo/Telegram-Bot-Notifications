package com.bronx.notification.model.entity;
import com.bronx.notification.model.audit.SoftDeletableAuditable;
import com.bronx.notification.model.enumz.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "notifications"
)
public class Notification extends SoftDeletableAuditable<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "chart_id")
    private String chartId;

    @Column(name = "bot_username",length = 150)
    private String botUsername;

    @Column(length = 60,name = "telegram_template_name")
    private String teleTemplateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TelegramMessageType type = TelegramMessageType.TEXT;

    @Column(name = "media_url", length = 2048)
    private String url;

    @Column(name = "media_caption", length = 1024)
    private String caption;
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "is_own_custom")
    private boolean isOwnCustom=true;

    @Column(name = "parse_mode",length = 30)
    @Enumerated(EnumType.STRING)
    private TelegramParseMode telegramParseMode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private JsonNode metaData;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.QUEUED;

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


    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "telegram_message_id")
    private String telegramMessageId; // Telegram's message ID after sending

    @Transient
    public boolean canRetry() {
        return retryCount < maxRetries &&
                status != NotificationStatus.SENT &&
                status != NotificationStatus.DELIVERED;
    }
}
