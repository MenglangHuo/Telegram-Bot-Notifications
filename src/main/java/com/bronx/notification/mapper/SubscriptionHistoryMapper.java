package com.bronx.notification.mapper;


import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryRequest;
import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryResponse;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.model.entity.SubscriptionHistory;
import com.bronx.notification.model.entity.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {Subscription.class, SubscriptionPlan.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubscriptionHistoryMapper {
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "plan", ignore = true)
    SubscriptionHistory toEntity(SubscriptionHistoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "plan", ignore = true)
    void updateEntityFromRequest(SubscriptionHistoryRequest request, @MappingTarget SubscriptionHistory entity);


    SubscriptionHistoryResponse toResponse(SubscriptionHistory entity);

    List<SubscriptionHistoryResponse> toResponses(List<SubscriptionHistory> entities);
}
