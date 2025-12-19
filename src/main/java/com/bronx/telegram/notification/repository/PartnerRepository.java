package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    @Query("select (count(p) > 0) from Partner p where p.partnerCode = ?1 and p.deletedAt is null")
    boolean existsByPartnerCodeAndDeletedAtIsNull(String partnerCode);

    @Query("select p from Partner p where p.id = ?1 and p.deletedAt is null")
    Optional<Partner> findByIdAndDeletedAtIsNull(Long partnerId);

    @Query("select count(p) from Partner p where p.id = ?1")
    Integer countTelegramBotById(Long partnerId);

    @Query("select count(p) from Partner p where p.id = ?1")
    Integer countSubscriptionsById(Long attr0);

    @Query("select p from Partner p where p.clientId = ?1 and p.secretKey = ?2 and p.deletedAt is null")
   Optional<Partner> findByClientIdAndSecretKey(String clientId, String secretKey);

    @Query("select p from Partner p where p.deletedAt is null")
    Page<Partner> findAllByDeletedAtIsNull(Pageable pageable);

    @Query("select p from Partner p where ((p.partnerName is null or p.partnerName=?1) or (p.partnerCode is null or p.partnerCode=?1) or ( p.contactEmail is null or p.contactEmail=?1))")
    Page<Partner> searchPartners(String search, Pageable pageable);


    Optional<Partner> findByPartnerCode(String partnerCode);

    Optional<Partner> findByClientId(String clientId);

    @Query("""
        SELECT p FROM Partner p
        WHERE p.status = 'ACTIVE'
        AND p.deletedAt IS NULL
    """)
    List<Partner> findAllActive();
}
