package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByPartnerIdAndEmployeeCode(Long partnerId, String employeeCode);

    @Query("select e from Employee e")
    Optional<Subscription> findActiveDivisionSubscription(Long id);

    boolean existsByPartnerIdAndEmail(Long partnerId, String employeeCode);

    boolean existsByPartnerIdAndEmployeeCode(Long partnerId, String email);
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPartnerIdAndEmail(Long partnerId, String email);

    Optional<Employee> findByTelegramUserId(String telegramUserId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.partner.id = :partnerId
        AND e.email = :email
        AND e.status = 'ACTIVE'
    """)
    Optional<Employee> findActiveByEmail(
            @Param("partnerId") Long partnerId,
            @Param("email") String email);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.department.id = :deptId
        AND e.isHeadOfDepartment = true
        AND e.status = 'ACTIVE'
    """)
    Optional<Employee> findHeadOfDepartment(@Param("deptId") Long departmentId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.division.id = :divId
        AND e.isHeadOfDivision = true
        AND e.status = 'ACTIVE'
    """)
    Optional<Employee> findHeadOfDivision(@Param("divId") Long divisionId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.department.id = :deptId
        AND e.isManager = true
        AND e.status = 'ACTIVE'
    """)
    List<Employee> findManagersByDepartment(@Param("deptId") Long departmentId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.organization.id = :orgId
        AND e.isHeadOfDivision = true
        AND e.status = 'ACTIVE'
    """)
    List<Employee> findDivisionHeadsByOrganization(@Param("orgId") Long organizationId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.partner.id = :partnerId
        AND e.managerCode = :managerCode
        AND e.status = 'ACTIVE'
    """)
    List<Employee> findSubordinates(
            @Param("partnerId") Long partnerId,
            @Param("managerCode") String managerCode);


}
