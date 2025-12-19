package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.audit.SoftDeletableAuditable;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"partner_id", "employee_code"}),
                @UniqueConstraint(columnNames = {"partner_id", "email"})
        },
        indexes = {
                @Index(name = "idx_employee_telegram", columnList = "telegram_user_id")
        })
public class Employee extends SoftDeletableAuditable<Long> {

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_unit_id", nullable = false)
    private OrganizationUnit organizationUnit;

    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @Column(name = "manager_code", length = 30)
    private String managerCode;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "role", length = 50)
    private String role;

    // ✅ FIX: Rename to match Telegram's user_id (it's a Long, not String)
    @Column(name = "telegram_user_id", length = 50)
    private String telegramUserId;

    @Column(name = "telegram_username", length = 100)
    private String telegramUsername;

    // ✅ ENHANCEMENT: Store chat_id separately (different from user_id)
    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;

    @Column(name = "contact", length = 30)
    private String contact;
//
    @Column(name = "employee_status", length = 10)
    @Enumerated(EnumType.STRING)
    private BotStatus status;

    @Column(name = "registered_at")
    private Instant registeredAt;
//
//    // ✅ ENHANCEMENT: Employee hierarchy level
//    @Column(name = "is_manager")
//    @Builder.Default
//    private Boolean isManager = false;
//
//    @Column(name = "is_head_of_department")
//    @Builder.Default
//    private Boolean isHeadOfDepartment = false;
//
//    @Column(name = "is_head_of_division")
//    @Builder.Default
//    private Boolean isHeadOfDivision = false;

//    @Transient
//    public String getHierarchyLevel() {
//        if (Boolean.TRUE.equals(isHeadOfDivision)) return "DIVISION_HEAD";
//        if (Boolean.TRUE.equals(isHeadOfDepartment)) return "DEPARTMENT_HEAD";
//        if (Boolean.TRUE.equals(isManager)) return "MANAGER";
//        return "EMPLOYEE";
//    }
    @Transient
    public boolean isRegisteredWithTelegram() {
        return telegramUserId != null && telegramChatId != null;
    }
}
