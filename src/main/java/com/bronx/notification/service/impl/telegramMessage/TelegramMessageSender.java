package com.bronx.notification.service.impl.telegramMessage;


import com.bronx.notification.configs.TelegramBotClientFactory;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.dto.telegramSender.TelegramMessageResponse;
import com.bronx.notification.model.enumz.TelegramMessageType;
import com.bronx.notification.service.impl.TelegramBotClient;
import com.bronx.notification.service.impl.telegramMessage.MessageType.*;
import com.bronx.notification.utils.MediaDownloader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramMessageSender {

    private final Map<Long, Map<TelegramMessageType, TelegramMessageStrategy>> strategiesCache =
            new ConcurrentHashMap<>();
    private final TelegramBotClientFactory botClientFactory;
    private final ObjectMapper objectMapper;
    private final MediaDownloader mediaDownloader;

    public TelegramMessageResponse sendMessage(
            Long botId,
            TelegramMessageRequest request
    ) throws Exception {
       try{


           TelegramBotClient botClient = botClientFactory.getClient(String.valueOf(botId));

           Map<TelegramMessageType, TelegramMessageStrategy> strategies = getStrategiesForBot(botClient);

           TelegramMessageStrategy strategy = strategies.get(request.getType());

           if (strategy == null) {
               String errorMsg = "No strategy found for message type: " + request.getType();
               log.error("❌ {}", errorMsg);
               return TelegramMessageResponse.error(
                       errorMsg,
                       null,
                       request.getChatId(),
                       request.getType()
               );
           }
           log.info("➡️ Sending {} message to chat_id: {}",
                   request.getType(), request.getChatId());

           return strategy.send(request);
       }catch (IllegalArgumentException e) {
           log.error("❌ Validation error: {}", e.getMessage());
           return TelegramMessageResponse.error(
                   e.getMessage(),
                   null,
                   request.getChatId(),
                   request.getType()
           );
       } catch (Exception e) {
           log.error("❌ Unexpected error sending message", e);
           return TelegramMessageResponse.error(
                   e.getMessage(),
                   null,
                   request.getChatId(),
                   request.getType()
           );
       }
    }


    private Map<TelegramMessageType,TelegramMessageStrategy> getStrategiesForBot(
            TelegramBotClient botClient
    ){
       String apiUrl=botClient.getApiUrl();

       return strategiesCache.computeIfAbsent(
               (long) System.identityHashCode(botClient),
               k -> createStrategies(apiUrl, botClient)
       );
    };


    private Map<TelegramMessageType,TelegramMessageStrategy> createStrategies(
            String apiUrl,
            TelegramBotClient httpClient
    ){
        Map<TelegramMessageType,TelegramMessageStrategy> strategyMap=new EnumMap<>(TelegramMessageType.class);
        registerStrategy(strategyMap, new TextMessage(apiUrl, httpClient, objectMapper));
        registerStrategy(strategyMap, new PhotoMessage(apiUrl, httpClient, objectMapper));
        registerStrategy(strategyMap, new VideoMessage(apiUrl, httpClient, objectMapper));
        registerStrategy(strategyMap, new AudioMessage(apiUrl, httpClient, objectMapper,mediaDownloader));
        registerStrategy(strategyMap, new DocumentMessage(apiUrl, httpClient, objectMapper));

        log.info("✅ Created {} message strategies", strategyMap.size());

        return strategyMap;

    }
    private void registerStrategy(
            Map<TelegramMessageType,TelegramMessageStrategy> strategyMap,
            TelegramMessageStrategy strategy
    ){
        strategyMap.put(strategy.getMessageType(),strategy);
    }


}
