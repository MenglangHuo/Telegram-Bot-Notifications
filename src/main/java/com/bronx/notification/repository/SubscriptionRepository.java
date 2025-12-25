package com.bronx.notification.repository;
import com.bronx.notification.model.entity.OrganizationUnit;
import com.bronx.notification.model.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByScopeId(Long orgUnitId);

    @Query("SELECT s FROM Subscription s WHERE s.scope.id = :orgId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByOrg(@Param("orgId") Long orgId);

    @Query("SELECT s FROM Subscription s WHERE s.scope.id = :orgId AND s.plan.id=:planId AND s.status = 'ACTIVE'")
    Optional<Object> findByScopeIdAndSubscriptionId(@Param("orgId") Long orgId,@Param("planId") Long planId);

    @Query("select s from Subscription s where (s.scope is null or s.scope = ?1) and s.status = 'ACTIVE'")
    List<Subscription> findActiveByScope(OrganizationUnit scope,Pageable pageable);

}