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

    // By company and chat name (using subscription or orgUnit)
    @Query("SELECT c FROM TelegramChannel c WHERE c.subscription.company.id = :companyId AND c.chatName = :chatName")
    Optional<TelegramChannel> findByCompanyIdAndChatName(@Param("companyId") Long companyId, @Param("chatName") String chatName);

    @Query("SELECT c FROM TelegramChannel c WHERE c.subscription.company.id = :companyId AND c.channelStatus = :status")
    List<TelegramChannel> findByCompanyIdAndChannelStatus(@Param("companyId") Long companyId, @Param("status") ChannelStatus status);

    // For scope-level (via path)
    @Query("SELECT c FROM TelegramChannel c WHERE c.organizationUnit.path LIKE :path% AND c.channelStatus = :status")
    List<TelegramChannel> findByPathStartingWithAndChannelStatus(@Param("path") String path, @Param("status") ChannelStatus status);


    List<TelegramChannel> findByOrganizationUnitIdAndChannelStatus(
            Long orgUnitId, ChannelStatus status);

}