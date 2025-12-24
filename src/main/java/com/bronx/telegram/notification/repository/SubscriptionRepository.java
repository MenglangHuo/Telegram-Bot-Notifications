package com.bronx.telegram.notification.repository;

import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import com.bronx.telegram.notification.model.enumz.SubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Page<Subscription> findAllByPartnerId(Long partnerId, Pageable pageable);

    Page<Subscription> findAllByPartnerIdAndStatus(
            Long partnerId,
            SubscriptionStatus status,
            Pageable pageable);
    List<Subscription> findAllByLastResetDateBefore(Instant date);

    @Query("""
        SELECT s FROM Subscription s 
        WHERE s.status = :status 
        AND s.endDate IS NOT NULL 
        AND s.endDate BETWEEN :start AND :end
    """)
    List<Subscription> findExpiringSubscriptions(
            @Param("status") SubscriptionStatus status,
            @Param("start") Instant start,
            @Param("end") Instant end);


    @Query("""
        SELECT s FROM Subscription s
        WHERE s.scope.id = :scopeId
        AND s.status = 'ACTIVE'
    """)
    Optional<Subscription> findActiveSubscriptionByScopeId(@Param("scopeId") Long scopeId);

    Optional<Subscription> findByCompanyIdAndScopeIsNull(Long companyId);

    @Query("select s from Subscription s where s.scope.id = ?1")
    Optional<Subscription> findByScopeId(Long scopeId);


    @Query("SELECT s FROM Subscription s WHERE s.scope.id = :orgUnitId " +
            "AND s.status = 'ACTIVE' AND s.deletedAt IS NULL")
    Optional<Subscription> findActiveSubscriptionByOrgUnit(Long orgUnitId);

    @Query("SELECT s FROM Subscription s WHERE s.company.id = :companyId " +
            "AND s.scope IS NULL AND s.status = 'ACTIVE' AND s.deletedAt IS NULL")
    Optional<Subscription> findActiveCompanySubscription(Long companyId);
}