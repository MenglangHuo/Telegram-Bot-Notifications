package com.bronx.telegram.notification.repository;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
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
public interface NotificationPersonalRepository extends JpaRepository<NotificationPersonal, Long> {
    List<NotificationPersonal> findByEmployeeIdAndStatus(Long employeeId, NotificationStatus status);

    @Query("SELECT n FROM NotificationPersonal n WHERE n.status IN :statuses " +
            "AND n.queuedAt < :before ORDER BY n.priority DESC, n.queuedAt ASC")
    List<NotificationPersonal> findPendingNotifications(
            @Param("statuses") List<NotificationStatus> statuses,
            @Param("before") Instant before,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM NotificationPersonal n WHERE n.employee.id = :employeeId " +
            "AND n.status = 'SENT' AND n.sentAt >= :since")
    long countSentNotificationsSince(@Param("employeeId") Long employeeId,
                                     @Param("since") Instant since);



    @Query("""
    select n from NotificationPersonal n
    join fetch n.employee
    where n.id = :id
""")
    Optional<NotificationPersonal> findWithEmployee(@Param("id") Long id);

    @Query("""
        SELECT COUNT(n) FROM NotificationPersonal n
        WHERE n.subscription.id = :subscriptionId
        AND n.queuedAt BETWEEN :start AND :end
    """)
    Long countBySubscriptionAndDateRange(
            @Param("subscriptionId") Long subscriptionId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("""
        SELECT n FROM NotificationPersonal n
        WHERE n.status IN ('QUEUED', 'PROCESSING')
        AND n.queuedAt < :timeout
    """)
    List<NotificationPersonal> findStuckNotifications(@Param("timeout") Instant timeout);



}
