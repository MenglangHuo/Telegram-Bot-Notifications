package com.bronx.notification.repository;
import com.bronx.notification.model.entity.OrganizationUnit;
import com.bronx.notification.model.entity.Subscription;
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

    Optional<Object> findByScope(OrganizationUnit scope);
}