package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification_channel")
public class NotificationChannel extends Notification {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telegram_channel_id", nullable = false)
    private TelegramChannel telegramChannel;


    // Media fields
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private TelegramMessageType mediaType = TelegramMessageType.TEXT;

    @Column(name = "media_url", length = 2048)
    private String mediaUrl;

    @Column(name = "media_file_id")
    private String mediaFileId; // Telegram file_id for reuse

    @Column(name = "media_caption", length = 1024)
    private String mediaCaption;

}
