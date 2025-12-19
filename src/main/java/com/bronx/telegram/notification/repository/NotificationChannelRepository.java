package com.bronx.telegram.notification.repository;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.enumz.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    List<NotificationChannel> findByTelegramChannelIdAndStatus(
            Long channelId, NotificationStatus status);

    @Query("SELECT n FROM NotificationChannel n WHERE n.status IN :statuses " +
            "AND n.queuedAt < :before ORDER BY n.priority DESC, n.queuedAt ASC")
    List<NotificationChannel> findPendingNotifications(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("before") Instant before,
            Pageable pageable);
    @Query("""
        SELECT n FROM NotificationChannel n
        JOIN FETCH n.telegramChannel
        WHERE n.id = :id
    """)
    Optional<NotificationChannel> findWithChannel(@Param("id") Long id);

    @Query("""
        SELECT COUNT(n) FROM NotificationChannel n
        WHERE n.subscription.id = :subscriptionId
        AND n.queuedAt BETWEEN :start AND :end
    """)
    Long countBySubscriptionAndDateRange(
            @Param("subscriptionId") Long subscriptionId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}