package com.bronx.notification.repository;
import com.bronx.notification.model.entity.OrganizationUnit;
import com.bronx.notification.model.enumz.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, Long> {

    // Hierarchy queries
    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.parent.id = :parentId " +
            "AND ou.deletedAt IS NULL ORDER BY ou.displayOrder, ou.name")
    List<OrganizationUnit> findChildrenByParentId(Long parentId);

    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.company.id = :companyId " +
            "AND ou.parent IS NULL AND ou.deletedAt IS NULL")
    List<OrganizationUnit> findRootUnitsByCompanyId(Long companyId);

    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.path LIKE CONCAT(:path, '%') " +
            "AND ou.deletedAt IS NULL ORDER BY ou.path")
    List<OrganizationUnit> findDescendantsByPath(String path);


    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.company.id = :companyId " +
            "AND ou.status = :status AND ou.deletedAt IS NULL")
    List<OrganizationUnit> findByCompanyAndStatus(Long companyId, UnitStatus status);

    // Search queries
    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.company.id = :companyId " +
            "AND (LOWER(ou.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(ou.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND ou.deletedAt IS NULL")
    List<OrganizationUnit> searchByCompanyId(Long companyId, String searchTerm);

    // Statistics queries
    @Query("SELECT COUNT(ou) FROM OrganizationUnit ou WHERE ou.parent.id = :parentId " +
            "AND ou.deletedAt IS NULL")
    Integer countChildren(Long parentId);

    @Query("SELECT COUNT(ou) FROM OrganizationUnit ou WHERE ou.company.id = :companyId " +
            "AND ou.deletedAt IS NULL")
    Integer countByCompany(Long companyId);

    @Query("SELECT SUM(ou.employeeCount) FROM OrganizationUnit ou " +
            "WHERE ou.path LIKE CONCAT(:path, '%') AND ou.deletedAt IS NULL")
    Integer sumEmployeeCountInSubtree(String path);


    // Leaf nodes
    @Query("SELECT ou FROM OrganizationUnit ou WHERE ou.company.id = :companyId " +
            "AND ou.isLeaf = true AND ou.deletedAt IS NULL")
    List<OrganizationUnit> findLeafNodes(Long companyId);

    // Path validation
    @Query("SELECT CASE WHEN COUNT(ou) > 0 THEN true ELSE false END " +
            "FROM OrganizationUnit ou WHERE ou.path = :path AND ou.deletedAt IS NULL")
    boolean existsByPath(String path);

    @Query("select o from OrganizationUnit o where o.company.id = ?1 and o.deletedAt is null")
    List<OrganizationUnit> findByCompanyId(Long id);


    @Query("select (count(o) > 0) from OrganizationUnit o where o.company.id = ?1 and o.code = ?2")
    boolean existsByCompanyIdAndUnitCode(Long companyId, String unitCode);
}

