package com.bronx.notification.mapper;


import com.bronx.notification.dto.notification.NotificationMessage;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.model.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

//    @Mapping(target = "mediaType", expression = "java(TelegramMessageType.valueOf(dto.getMessageType().toUpperCase()))")
    @Mapping(target = "teleTemplateName", source = "templateName")
    @Mapping(target = "queuedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "telegramParseMode", source = "parseMode")
    Notification toEntity(TelegramMessageRequest dto);

    @Mapping(target = "subscriptionId",source = "subscription.id")
    NotificationMessage toResponse(Notification notification);
}
