package com.bronx.telegram.notification.repository;
import com.bronx.telegram.notification.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByPartnerIdAndEmployeeCode(Long partnerId, String employeeCode);

    boolean existsByPartnerIdAndEmail(Long partnerId, String employeeCode);

    boolean existsByPartnerIdAndEmployeeCode(Long partnerId, String email);
    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.organizationUnit.id = :orgUnitId " +
            "AND (e.role LIKE '%MANAGER%' OR e.role LIKE '%HEAD%') " +
            "AND e.status = 'ACTIVE' AND e.deletedAt IS NULL")
    List<Employee> findManagersInOrgUnit(Long orgUnitId);

}
