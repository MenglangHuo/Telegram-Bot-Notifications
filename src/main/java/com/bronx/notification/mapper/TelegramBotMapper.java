package com.bronx.notification.mapper;

import com.bronx.notification.dto.telegrambot.BotRequest;
import com.bronx.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.notification.model.entity.TelegramBot;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {SubscriptionMapper.class, PartnerMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface TelegramBotMapper {
    TelegramBotResponse toResponse(TelegramBot telegramBot);
    TelegramBot toEntity(BotRequest request);
}
