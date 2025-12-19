package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);

    Optional<Company> findByCodeAndDeletedAtIsNull(String code);

    List<Company> findByPartnerIdAndDeletedAtIsNull(Long partnerId);

    List<Company> findByPartnerId(Long partnerId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c FROM Company c WHERE c.partner.id = :partnerId AND c.deletedAt IS NULL")
    List<Company> findActiveCompaniesByPartnerId(@Param("partnerId") Long partnerId);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.partner.id = :partnerId AND c.deletedAt IS NULL")
    long countByPartnerId(@Param("partnerId") Long partnerId);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Company> searchCompanies(@Param("search") String search, Pageable pageable);

}
