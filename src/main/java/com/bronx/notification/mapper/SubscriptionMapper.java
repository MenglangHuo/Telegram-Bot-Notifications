package com.bronx.notification.mapper;
import com.bronx.notification.dto.subscription.SubscriptionMainResponse;
import com.bronx.notification.dto.subscription.SubscriptionRequest;
import com.bronx.notification.dto.subscription.SubscriptionResponse;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanRequest;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.model.entity.SubscriptionPlan;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {OrganizationUnitMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface SubscriptionMapper {
    @Mapping(target = "scope", ignore = true) // Handled in service
    @Mapping(target = "plan", ignore = true) // Handled in service
    Subscription toEntity(SubscriptionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scope", ignore = true)
    @Mapping(target = "plan", ignore = true)
    void updateEntityFromRequest(SubscriptionRequest request, @MappingTarget Subscription entity);


//    @Mapping(source = "plan.id", target = "plan")
    SubscriptionResponse toResponse(Subscription entity);

    SubscriptionMainResponse toMainResponse(Subscription entity);

    List<SubscriptionResponse> toResponses(List<Subscription> entities);

}
