package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
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
@Table(name = "company")
public class Company extends SoftDeletableAuditable<Long> {
    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "name", nullable = false,length = 100)
    private String name;

    @Column(name = "code", nullable = false,length = 70)
    private String code;

    private String description;
}
