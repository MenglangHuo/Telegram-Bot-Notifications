package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;


@Setter
@Getter
@Entity
@Table(name = "webhook")
@AllArgsConstructor
@NoArgsConstructor
public class Webhook extends SoftDeletableAuditable<Long> {
    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    private TelegramBot bot;

    @Column(name = "update_id", nullable = false)
    private Long updateId;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "message_type", length = 50)
    private String messageType; // text, command, callback_query, etc.

    @Column(name = "command", length = 50)
    private String command; // /register, /start, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb")
    private JsonNode content; // Full webhook payload

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(name = "retry_count")
    private Integer retryCount = 0;
}
