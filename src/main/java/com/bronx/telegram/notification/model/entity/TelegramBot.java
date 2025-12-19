package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "telegram_bots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subscription_id", "bot_username"})
        })
public class TelegramBot extends SoftDeletableAuditable<Long> {

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "bot_token", nullable = false)
    private String botToken;

    @Column(name = "bot_username", nullable = false)
    private String botUsername;

    @Column(name = "bot_name")
    private String botName;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BotStatus status = BotStatus.ACTIVE;

    @Column(name = "rate_limit_per_second")
    @Builder.Default
    private Integer rateLimitPerSecond = 25;

    @Column(name = "webhook_verified")
    @Builder.Default
    private Boolean webhookVerified = false;

    // ✅ FIX: Add bot configuration
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bot_config", columnDefinition = "jsonb")
    private JsonNode botConfig;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @Column(name = "error_count")
    @Builder.Default
    private Integer errorCount = 0;

    // Transient - not stored in DB
    @Transient
    private TelegramBot botInstance;

    @Transient
    public boolean isHealthy() {
        if (status != BotStatus.ACTIVE) return false;
        if (!Boolean.TRUE.equals(webhookVerified)) return false;
        if (errorCount != null && errorCount > 10) return false; // Too many errors
        return true;
    }
}
