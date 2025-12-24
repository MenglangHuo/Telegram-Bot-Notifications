package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.UnitStatus;
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
                @UniqueConstraint(columnNames = {"company_id", "unit_code"}),
                @UniqueConstraint(columnNames = {"company_id", "path"})
        },
        indexes = {
                @Index(name = "idx_org_unit_path", columnList = "path"),
                @Index(name = "idx_org_unit_parent", columnList = "parent_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUnit extends SoftDeletableAuditable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrganizationUnit parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrganizationUnit> children = new ArrayList<>();

    @Column(nullable = false, length = 200)
    private String unitName;

    @Column(nullable = false, length = 50)
    private String unitCode;

    @Column(length = 200)
    private String path;

    @Column(nullable = false)
    @Builder.Default
    private Integer hierarchyLevel = 0;

    @Column(length = 1000)
    private String fullPath;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private UnitStatus status = UnitStatus.ACTIVE;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_email", length = 100)
    private String managerEmail;

    @Column(name = "manager_phone", length = 30)
    private String managerPhone;

    @Column(name = "is_leaf")
    @Builder.Default
    private Boolean isLeaf = false;

    @Column(name = "employee_count")
    @Builder.Default
    private Integer employeeCount = 0;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "depth")
    private Integer depth = 0;

    @Transient
    public boolean isRoot() {
        return parent == null;
    }

    @Transient
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
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

    // ✅ FIXED: Use @PostPersist for path computation
    @PostPersist
    private void computePathAfterPersist() {
        updatePath();
    }

    @PreUpdate
    private void updateComputedFields() {
        this.isLeaf = (children == null || children.isEmpty());
        // Only update path if parent changed
        if (parent != null && !path.startsWith(parent.getPath())) {
            updatePath();
        }
    }

    // ✅ NEW: Separate method for path computation
    public void updatePath() {
        if (getId() == null) {
            throw new IllegalStateException("Cannot compute path before entity is persisted");
        }

        if (parent == null) {
            this.path = "/" + getId() + "/";
            this.hierarchyLevel = 0;
            this.depth = 0;
            this.fullPath = unitName;
        } else {
            this.path = parent.getPath() + getId() + "/";
            this.hierarchyLevel = parent.getHierarchyLevel() + 1;
            this.depth = parent.getDepth() + 1;
            this.fullPath = parent.getFullPath() + " > " + unitName;
        }
    }
}