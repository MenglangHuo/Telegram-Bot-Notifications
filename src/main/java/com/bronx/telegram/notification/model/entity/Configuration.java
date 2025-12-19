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

@Entity
@Table(name = "configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Configuration extends SoftDeletableAuditable<Long> {

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    // Only ONE of these should be set
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 50)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "values", columnDefinition = "jsonb")
    private JsonNode values; // Additional meta data

    private String description;
}
