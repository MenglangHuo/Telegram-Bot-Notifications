package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "telegram_template")
public class TelegramTemplate extends SoftDeletableAuditable<Long> {

    @Column(length = 60, nullable = false, unique = true)
    private String name;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

}
