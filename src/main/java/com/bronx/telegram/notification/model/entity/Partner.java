package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partner extends SoftDeletableAuditable<Long> {
    @Column(name = "partner_name", nullable = false,length = 100)
    private String partnerName;

    @Column(name = "partner_code", nullable = false, unique = true)
    private String partnerCode;

    @Column(name = "client_id", nullable = false, unique = true,length=70)
    private String clientId;

    @Column(name = "secret_key", nullable = false)
    private String secretKey; // Encrypted

    @Column(name = "subscription_tier",length = 30)
    private String subscriptionTier = "BASIC"; // BASIC, PREMIUM, ENTERPRISE

    @Column(name = "max_organizations")
    private Integer maxOrganizations = 1;

    @Column(name = "max_bots")
    private Integer maxBots = 5;

    @Column(name = "status",length = 20)
    private String status = "ACTIVE";

    @Column(name = "contact_name",length = 70)
    private String contactName;

    @Column(name = "contact_email",length = 70)
    private String contactEmail;

    @Column(name = "contact",length = 30)
    private String contact;
}
