package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"partner_id", "organization_code"})
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends SoftDeletableAuditable<Long> {
    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "organization_name", nullable = false,length = 100)
    private String organizationName;

    @Column(name = "organization_code", nullable = false,length = 70)
    private String organizationCode;

    @Column(name = "hierarchy_enabled")
    private Boolean hierarchyEnabled = true;

}
