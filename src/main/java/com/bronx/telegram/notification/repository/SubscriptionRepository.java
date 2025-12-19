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

import java.io.Serializable;
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

    List<Subscription> findAllByStatus(SubscriptionStatus status);

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
        WHERE s.subscriptionType = :type
        AND s.organization.id = :orgId
        AND s.status = 'ACTIVE'
    """)
    List<Subscription> findByTypeAndOrganization(
            @Param("type") SubscriptionType type,
            @Param("orgId") Long organizationId);

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.subscriptionType = 'DIVISION'
        AND s.division.id = :divisionId
        AND s.status = 'ACTIVE'
    """)
    Optional<Subscription> findActiveDivisionSubscription(@Param("divisionId") Long divisionId);

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.subscriptionType = 'DEPARTMENT'
        AND s.department.id = :deptId
        AND s.status = 'ACTIVE'
    """)
    Optional<Subscription> findActiveDepartmentSubscription(@Param("deptId") Long departmentId);

    @Query("""
       SELECT s FROM Subscription s
       WHERE s.subscriptionName = 'ORGANIZATION'
       AND s.organization.id =:organizeId
       AND s.status ='ACTIVE'
""")
    Optional<Subscription> findActiveOrganizationSubscription(@Param("organizeId") Long organizeId);

}
