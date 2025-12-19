package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.entity.TelegramChannel;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.ChannelStatus;
import com.bronx.telegram.notification.model.enumz.ChartType;
import com.bronx.telegram.notification.repository.SubscriptionRepository;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.repository.TelegramChannelRepository;
import com.bronx.telegram.notification.service.SubscriptionValidateService;
import com.bronx.telegram.notification.service.TelegramChannelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TelegramChannelServiceImpl implements TelegramChannelService {

    private final TelegramChannelRepository channelRepository;
    private final TelegramBotRepository botRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionValidateService validationService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public void handleAddNewChat(Webhook webhook) {

        try {
            JsonNode root = objectMapper.readTree(webhook.getContent().traverse());
            JsonNode chatMember = root.get("my_chat_member");

            if (chatMember == null) {
                log.warn("No my_chat_member in webhook");
                return;
            }

            JsonNode chatNode = chatMember.get("chat");
            JsonNode newMember = chatMember.get("new_chat_member");

            String chatType = chatNode.get("type").asText();
            String chatId = chatNode.get("id").asText();
            String chatTitle = chatNode.has("title") ?
                    chatNode.get("title").asText() : "Private Chat";
            String newStatus = newMember.get("status").asText();

            // Only process channels/groups where bot became admin/member
            if (!List.of("channel", "group", "supergroup").contains(chatType)) {
                return;
            }

            if (!List.of("administrator", "member").contains(newStatus)) {
                log.info("Bot removed from chat {}", chatId);
                handleBotRemoved(webhook.getBot(), chatId);
                return;
            }

            // Check if channel already exists
            Optional<TelegramChannel> existing = channelRepository
                    .findByBotIdAndChatId(webhook.getBot().getId(), chatId);

            if (existing.isPresent()) {
                log.info("Channel {} already registered", chatTitle);
                return;
            }

            // Validate subscription limits
            TelegramBot bot = botRepository.findById(webhook.getBot().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Bot not found"));

            if (!validationService.canAddChannel(bot.getSubscription())) {
                log.error("Cannot add channel: subscription limit reached");
                // TODO: Send message to chat explaining limit reached
                return;
            }

            // Determine bot permissions
            boolean isBotAdmin = "administrator".equals(newStatus);
            boolean canPinMessages = false;

            if (isBotAdmin && newMember.has("can_pin_messages")) {
                canPinMessages = newMember.get("can_pin_messages").asBoolean();
            }

            // Create channel entity
            TelegramChannel channel = new TelegramChannel();
            channel.setBot(bot);
            channel.setSubscription(bot.getSubscription());
            channel.setChatId(chatId);
            channel.setChatType(ChartType.valueOf(chatType.toUpperCase()));
            channel.setChatName(chatTitle);
            channel.setCanPinMessages(canPinMessages);
            channel.setChannelStatus(ChannelStatus.ACTIVE);
            channel.setVerifiedAt(Instant.now());

            // Set scope based on subscription
            Subscription subscription = bot.getSubscription();
            channel.setOrganizationUnit(subscription.getScope());

            channelRepository.save(channel);

            log.info("✅ Registered {} '{}' for subscription {}",
                    chatType, chatTitle,
                    subscription.getId());

        } catch (Exception e) {
            log.error("Failed to handle add new chat", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TelegramChannel> getChannelsForScope(Subscription subscription) {
        if (subscription.getScope() == null) {
            return channelRepository.findByCompanyIdAndChannelStatus(
                    subscription.getCompany().getId(), ChannelStatus.ACTIVE);
        } else {
            // Find channels in scope and descendants
            return channelRepository.findByPathStartingWithAndChannelStatus(
                    subscription.getScope().getPath(), ChannelStatus.ACTIVE);
        }
    }
    private void handleBotRemoved(TelegramBot bot, String chatId) {
        channelRepository.findByBotIdAndChatId(bot.getId(), chatId)
                .ifPresent(channel -> {
                    channel.setChannelStatus(ChannelStatus.INACTIVE);
                    channel.setDeletedAt(Instant.now());
                    channelRepository.save(channel);
                    log.info("Marked channel {} as inactive", channel.getChatName());
                });
    }

//    @Override
//    public List<TelegramChannel> getChannelsForScope(Subscription subscription) {
//        if (subscription.getScope() == null) {
//            // Company-wide subscription - get all channels for the company
//            return channelRepository.findBySubscriptionIdAndChannelStatus(
//                    subscription.getId(), ChannelStatus.ACTIVE);
//        }
//
//        // Get channels for specific org unit
//        return channelRepository.findByOrganizationUnitIdAndChannelStatus(
//                subscription.getScope().getId(), ChannelStatus.ACTIVE);
//    }
//
//    public List<TelegramChannel> getChannelsForOrgUnitHierarchy(OrganizationUnit orgUnit) {
//        List<TelegramChannel> channels = new ArrayList<>();
//
//        // Get channels for this org unit and all parent units
//        OrganizationUnit currentUnit = orgUnit;
//        while (currentUnit != null) {
//            channels.addAll(channelRepository.findByOrganizationUnitIdAndChannelStatus(
//                    currentUnit.getId(), ChannelStatus.ACTIVE));
//            currentUnit = currentUnit.getParent();
//        }
//
//        return channels;
//    }
//
//    private void handleBotRemoved(TelegramBot bot, String chatId) {
//        channelRepository.findByBotIdAndChatId(bot.getId(), chatId)
//                .ifPresent(channel -> {
//                    channel.setChannelStatus(ChannelStatus.INACTIVE);
//                    channel.setDeletedAt(Instant.now());
//                    channelRepository.save(channel);
//                    log.info("🚫 Marked channel {} as inactive", channel.getChatName());
//                });
//    }
}

