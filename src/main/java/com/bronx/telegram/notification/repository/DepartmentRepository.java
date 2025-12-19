package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Department;
import com.bronx.telegram.notification.model.entity.Division;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("select (count(d) > 0) from Department d where d.organization.id = ?1 and d.departmentName = ?2 and d.deletedAt is null")
    boolean existsByOrganizationIdAndDepartmentName(Long organizationId, String departmentName);

    // TODO
    @Query("select d from Department d where d.division = ?1  and d.deletedAt is null") //to do
    Page<Department> searchDepartmentsByDivision(Long divisionId, String query, Pageable pageable);

    List<Department> findByOrganizationId(Long organizationId);

    List<Department> findByDivisionId(Long divisionId);

    Optional<Department> findByOrganizationIdAndDepartmentCode(
            Long organizationId,
            String departmentCode);
}
