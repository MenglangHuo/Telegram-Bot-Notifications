package com.bronx.telegram.notification.repository;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramBotRepository extends JpaRepository<TelegramBot, Long> {
    List<TelegramBot> findByStatus(BotStatus status);

    Optional<TelegramBot> findByBotUsername(String username);

    @Query("""
        SELECT COUNT(b) FROM TelegramBot b
        WHERE b.subscription.id = :subscriptionId
        AND b.status = 'ACTIVE'
        AND b.deletedAt IS NULL
    """)
    Integer countActiveBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    Optional<TelegramBot> findFirstBySubscriptionIdAndStatus(
            Long subscriptionId,
            BotStatus status);

    @Query("""
        SELECT b FROM TelegramBot b
        WHERE b.subscription.id = :subscriptionId
        AND b.status = 'ACTIVE'
        AND b.deletedAt IS NULL
        ORDER BY b.createdAt ASC
    """)
    List<TelegramBot> findActiveBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Query("""
        SELECT b FROM TelegramBot b
        WHERE b.partner.id = :partnerId
        AND b.status = :status
        AND b.deletedAt IS NULL
    """)
    List<TelegramBot> findByPartnerIdAndStatus(
            @Param("partnerId") Long partnerId,
            @Param("status") BotStatus status);

    @Query("""
SELECT b FROM TelegramBot b
JOIN FETCH b.subscription
WHERE b.status = :status
""")
    List<TelegramBot> findActiveBotsWithSubscription(@Param("status") BotStatus status);

}

