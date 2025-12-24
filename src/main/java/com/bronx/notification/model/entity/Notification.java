package com.bronx.notification.model.entity;
import com.bronx.notification.model.audit.SoftDeletableAuditable;
import com.bronx.notification.model.enumz.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notification_status", columnList = "status"),
                @Index(name = "idx_notification_priority", columnList = "priority"),
                @Index(name = "idx_notification_queued_at", columnList = "queued_at")
        }
)
public class Notification extends SoftDeletableAuditable<Long> implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id")
    private OrganizationUnit organizationUnit;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "location",length = 100)
    private String location;

    @Column(name = "is_own_custom")
    private boolean isOwnCustom=true;

    @Column(name = "parse_mode",length = 30)
    @Enumerated(EnumType.STRING)
    private TelegramParseMode telegramParseMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private TelegramMessageType mediaType = TelegramMessageType.TEXT;

    @Column(name = "media_url", length = 2048)
    private String mediaUrl;

    @Column(name = "media_caption", length = 1024)
    private String mediaCaption;

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
