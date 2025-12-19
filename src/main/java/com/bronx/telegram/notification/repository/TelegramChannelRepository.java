package com.bronx.telegram.notification.repository;
import com.bronx.telegram.notification.model.entity.TelegramChannel;
import com.bronx.telegram.notification.model.enumz.ChannelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramChannelRepository extends JpaRepository<TelegramChannel, Long> {
    Optional<TelegramChannel> findByBotIdAndChatId(Long botId, String chatId);

    @Query("""
        SELECT COUNT(c) FROM TelegramChannel c
        WHERE c.subscription.id = :subscriptionId
        AND c.channelStatus = 'ACTIVE'
        AND c.deletedAt IS NULL
    """)
    Integer countActiveBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    List<TelegramChannel> findByOrganizationIdAndChannelStatus(
            Long organizationId,
            ChannelStatus status);

    @Query("select t from TelegramChannel t where t.organization.id = ?1 and t.chatName = ?2")
    Optional<TelegramChannel> findByOrganizationIdAndChatName(
            Long organizationId,
            String channelName
    );

    List<TelegramChannel> findByDivisionIdAndChannelStatus(
            Long divisionId,
            ChannelStatus status);

    List<TelegramChannel> findByDepartmentIdAndChannelStatus(
            Long departmentId,
            ChannelStatus status);

    @Query("""
        SELECT c FROM TelegramChannel c
        WHERE c.subscription.id = :subscriptionId
        AND c.channelStatus = 'ACTIVE'
        AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC
    """)
    List<TelegramChannel> findActiveBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

    @Query("""
        SELECT c FROM TelegramChannel c
        WHERE c.bot.id = :botId
        AND c.channelStatus IN ('ACTIVE', 'VERIFICATION_PENDING')
        AND c.deletedAt IS NULL
    """)
    List<TelegramChannel> findActiveByBotId(@Param("botId") Long botId);
}