package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.enumz.TelegramMessageType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification_personal")
public class NotificationPersonal extends Notification implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable=false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_manager_id")
    private Employee employeeManager;

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
