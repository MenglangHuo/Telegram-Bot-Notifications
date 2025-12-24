package com.bronx.notification.model.entity;

import com.bronx.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"partner_id", "code"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company extends SoftDeletableAuditable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 70)
    private String code;

    private String description;
}
