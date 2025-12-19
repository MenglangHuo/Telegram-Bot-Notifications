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
@Table(name = "divisions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"organization_id", "division_code"})
        }
)
public class Division extends SoftDeletableAuditable<Long> {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    @Column(name = "division_name", nullable = false,length = 100)
    private String divisionName;

    @Column(name = "division_code", nullable = false,length=50)
    private String divisionCode;

    @Column(name = "manager_name", nullable = false,length = 50)
    private String managerName;

    @Column(name = "manager_email",length = 70)
    private String managerEmail;

}
