package com.bronx.notification.repository;
import com.bronx.notification.model.entity.SubscriptionPlan;
import com.bronx.notification.model.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    Optional<Webhook> findByUpdateId(Long updateId);

    List<Webhook> findByProcessedFalse();

    @Query("SELECT w FROM Webhook w WHERE w.processed = false " +
            "AND w.retryCount < 3 ORDER BY w.createdAt ASC")
    List<Webhook> findUnprocessedWebhooks(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Webhook w WHERE w.createdAt < :before AND w.processed = true")
    int deleteOldProcessedWebhooks(@Param("before") Instant before);

    boolean existsByUpdateId(Long updateId);

    @Query("""
        SELECT w FROM Webhook w
        WHERE w.bot.id = :botId
        AND w.processed = false
        ORDER BY w.createdAt ASC
    """)
    List<Webhook> findUnprocessedByBot(@Param("botId") Long botId);

    @Query("""
        SELECT w FROM Webhook w
        WHERE w.processed = false
        AND w.retryCount < 3
        AND w.createdAt < :timeout
        ORDER BY w.createdAt ASC
    """)
    List<Webhook> findStuckWebhooks(@Param("timeout") Instant timeout);

    @Repository
    interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
        Optional<SubscriptionPlan> findByCode(String code);
        boolean existsByCode(String code);
    }
}
