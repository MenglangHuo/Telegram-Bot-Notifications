package com.bronx.telegram.notification.repository;

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
public interface DivisionRepository extends JpaRepository<Division, Long> {


    @Query("select d from Division d where d.id = ?1 and d.deletedAt is null")
    Optional<Division> findByIdAndDeletedAtIsNull(Long aLong);


    @Query("""
            select (count(d) > 0) from Division d
            where d.organization.id = ?1 and d.divisionCode = ?2 and d.divisionName = ?3 and d.deletedAt is null""")
    boolean existsByOrganizationIdAndDivisionCodeAndDivisionName(Long organizationId, String divisionCode, String divisionName);

    @Query("select d from Division d where d.organization = ?1 and ((d.divisionCode is null or d.divisionCode=?2) or (d.divisionName is null or d.divisionName=?2) )")
    Page<Division> searchDivisionsByOrganization(Long organizationId, String query, Pageable pageable);


    @Query("select d from Division d where d.organization.id = ?1")
    List<Division> findByOrganizationId(Long organizationId);

    @Query("select d from Division d where d.organization.id = ?1 and d.divisionCode = ?2")
    Optional<Division> findByOrganizationIdAndDivisionCode(
            Long organizationId,
            String divisionCode);
}
