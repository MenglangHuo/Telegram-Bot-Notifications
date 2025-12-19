package com.bronx.telegram.notification.model.entity;
import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department extends SoftDeletableAuditable<Long> {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private Division division;

    @Column(name = "department_name", nullable = false,length = 100)
    private String departmentName;

    @Column(name = "department_code", nullable = false,length = 50)
    private String departmentCode;

    @Column(name = "head_name",length = 60)
    private String headName;

    @Column(name = "head_email",length = 70)
    private String headEmail;

//    @Column(name = "status")
//    private String status = "ACTIVE";
}
