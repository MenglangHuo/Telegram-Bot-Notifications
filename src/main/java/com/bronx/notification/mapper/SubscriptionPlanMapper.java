package com.bronx.notification.mapper;

import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanRequest;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.model.entity.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface SubscriptionPlanMapper {
    SubscriptionPlan toEntity(SubscriptionPlanRequest request);

    @Mapping(target = "id", ignore = true) // Ignore id for update mapping
    void updateEntityFromRequest(SubscriptionPlanRequest request, @MappingTarget SubscriptionPlan entity);

    SubscriptionPlanResponse toResponse(SubscriptionPlan entity);

    List<SubscriptionPlanResponse> toResponses(List<SubscriptionPlan> entities);
}
