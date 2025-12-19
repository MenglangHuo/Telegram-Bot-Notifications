package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.UnitStatus;
import com.bronx.telegram.notification.model.enumz.UnitType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "organization_units",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"partner_id", "unit_code"}),
                @UniqueConstraint(columnNames = {"partner_id", "path"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUnit extends SoftDeletableAuditable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Company partner;

    // Self-referencing for tree structure
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrganizationUnit parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<OrganizationUnit> children = new ArrayList<>();

    @Column(name = "unit_name", nullable = false, length = 200)
    private String unitName;

    @Column(name = "unit_code", nullable = false, length = 50)
    private String unitCode;

    @Column(name = "unit_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    // Materialized Path Pattern for efficient queries
    @Column(name = "path", nullable = false, length = 500)
    private String path; // e.g., "/1/5/23/" for quick hierarchy queries

    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel; // 0 = root (organization), 1 = first level, etc.

    @Column(name = "full_path", length = 1000)
    private String fullPath; // Human-readable: "ACME/Sales/Enterprise/NY Office"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Contact information
    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 100)
    private String managerEmail;

    @Column(name = "manager_phone", length = 30)
    private String managerPhone;


    // Status and settings
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private UnitStatus status = UnitStatus.ACTIVE;

    @Column(name = "is_leaf")
    private Boolean isLeaf = false; // True if no children allowed

    @Column(name = "employee_count")
    private Integer employeeCount = 0;

    // Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata; // Additional custom fields

    @Column(name = "display_order")
    private Integer displayOrder = 0; // For sorting

    // Helper methods
    @Transient
    public boolean isRoot() {
        return parent == null;
    }

    @Transient
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Transient
    public String getPathArray() {
        // Returns ["1", "5", "23"] for path "/1/5/23/"
        return path.replaceAll("^/|/$", "");
    }

    @Transient
    public List<Long> getAncestorIds() {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return Collections.emptyList();
        }
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @PrePersist
    @PreUpdate
    private void updateComputedFields() {
        // Update path
        if (parent == null) {
            this.path = "/" + getId() + "/";
            this.hierarchyLevel = 0;
            this.fullPath = unitName;
        } else {
            this.path = parent.getPath() + getId() + "/";
            this.hierarchyLevel = parent.getHierarchyLevel() + 1;
            this.fullPath = parent.getFullPath() + "/" + unitName;
        }

        // Update leaf status
        this.isLeaf = (children == null || children.isEmpty());
    }
}
