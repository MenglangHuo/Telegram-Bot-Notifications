package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import com.bronx.notification.model.enumz.BotStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Transient
    public boolean isHealthy() {
        if (status != BotStatus.ACTIVE) return false;
        return true;
    }

    /**
     * Check if bot is ready to send messages
     */
    @Transient
    public boolean isReady() {
        return subscription != null && subscription.isValid();
    }
}
