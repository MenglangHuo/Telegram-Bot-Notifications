package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.model.entity.TelegramChannel;
import com.bronx.telegram.notification.model.entity.Webhook;

import java.util.List;

public interface TelegramChannelService {
    void handleAddNewChat(Webhook webhook);
    List<TelegramChannel> getChannelsForScope(Subscription subscription);
}
