package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("select count(o) from Organization o where o.partner.id = ?1")
    Integer countOrganizationsByPartnerId(Long partnerId);


    @Query("select o from Organization o where o.id = ?1 and o.deletedAt is null")
    Optional<Organization> findOrganizationById(Long id);

    @Query("""
            select (count(o) > 0) from Organization o
            where o.partner.id = ?1 and o.organizationCode = ?2 and o.organizationName=?3 and o.deletedAt is null""")
    boolean existsByPartnerIdAndOrganizationCodeAndDeletedAtIsNull(Long partnerId, String organizationCode,String organizationName);


    @Query("select o from Organization o where o.partner.id = ?1 and o.deletedAt is null")
    Page<Organization> findAllByPartnerId(Long partnerId, Pageable pageable);

    Optional<Organization> findByPartnerIdAndOrganizationCode(
            Long partnerId,
            String organizationCode);

    List<Organization> findByPartnerId(Long partnerId);

    @Query("""
        SELECT o FROM Organization o
        WHERE o.partner.id = :partnerId
        AND o.deletedAt IS NULL
    """)
    List<Organization> findActiveByPartnerId(@Param("partnerId") Long partnerId);

}
