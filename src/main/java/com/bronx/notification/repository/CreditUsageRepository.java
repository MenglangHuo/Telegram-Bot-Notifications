package com.bronx.notification.repository;

import com.bronx.notification.model.entity.CreditUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CreditUsageRepository extends JpaRepository<CreditUsage, Long> {
    List<CreditUsage> findBySubscriptionId(Long subscriptionId);
    @Query("SELECT SUM(c.usedCredits) FROM CreditUsage c WHERE c.subscription.id = :subscriptionId")
    Long sumUsedCreditsBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
    @Query("SELECT SUM(c.usedCredits) FROM CreditUsage c " +
            "WHERE c.subscription.id = :subscriptionId AND c.usedAt BETWEEN :start AND :end")
    Long sumUsedCreditsBySubscriptionIdAndDateRange(
            @Param("subscriptionId") Long subscriptionId,
            @Param("start") Instant start,
            @Param("end") Instant end);
    List<CreditUsage> findByBatchId(String batchId);

}
