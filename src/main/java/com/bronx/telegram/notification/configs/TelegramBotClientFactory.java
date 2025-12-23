package com.bronx.telegram.notification.configs;

import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.service.impl.TelegramBotClient;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBotClientFactory {
    private final Map<Long, TelegramBotClient> clientsCaches=new ConcurrentHashMap<>();
    private final TelegramBotRepository telegramBotRepository;
    private final AtomicInteger activeClients = new AtomicInteger(0);

    public TelegramBotClient getClient(String botId){
        return clientsCaches.computeIfAbsent(Long.valueOf(botId), id->{
            TelegramBot telegramBot=telegramBotRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Telegram bot not found"));
            log.info("✅ Created TelegramBotClient for bot id: {}", id);
            if (telegramBot.getStatus() != BotStatus.ACTIVE) {
                throw new IllegalStateException("Bot " + id + " is not active");
            }
            log.info("✅ Created TelegramBotClient for bot id: {}", id);
            activeClients.incrementAndGet();
            return new TelegramBotClient(telegramBot.getBotToken());
        });
    }

    public void invalidateClient(Long botId) {
        TelegramBotClient client = clientsCaches.remove(botId);
        if (client != null) {
            client.shutdown();
            log.info("🗑️ Invalidated client for bot ID: {}", botId);
        }
    }
    public void refreshClient(Long botId) {
        invalidateClient(botId);
        getClient(String.valueOf(botId)); // Will create new client
    }
    public Collection<TelegramBotClient> getAllClients() {
        return clientsCaches.values();
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down all TelegramBotClients...");
        clientsCaches.values().forEach(TelegramBotClient::shutdown);
        clientsCaches.clear();
        activeClients.set(0);
        log.info("✅ All clients shutdown completed");
    }


}
