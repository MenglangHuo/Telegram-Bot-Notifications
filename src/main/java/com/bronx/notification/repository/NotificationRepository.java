package com.bronx.notification.repository;

import com.bronx.notification.dto.notification.NotificationDetailProjection;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.enumz.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(NotificationStatus status);

    @Query(value = """
        SELECT 
            n.id as notificationId,
            n.bot_username as botName,
            sub.name as subscriptionName,
            org.name as organizationName,
            sub.remaining_credits as remainCredit,
            com.name as company,
            part.partner_name as partner
        FROM notifications n 
        INNER JOIN subscriptions sub ON sub.id = n.subscription_id 
        INNER JOIN organization_units org ON org.id = sub.org_unit_id 
        INNER JOIN companies com ON com.id = org.company_id 
        INNER JOIN partners part ON part.id = com.partner_id
        WHERE n.id = :notificationId
        """, nativeQuery = true)
    Optional<NotificationDetailProjection> findNotificationDetailById(@Param("notificationId") Long notificationId);


    @Query("SELECT n FROM Notification n WHERE n.subscription.id = :subscriptionId ORDER BY n.createdAt DESC")
    List<Notification> findBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status WHERE n.id = :id")

    int updateStatus(@Param("id") Long id, @Param("status") NotificationStatus status);

    long countBySubscriptionIdAndStatus(Long subscriptionId, NotificationStatus status);
}
