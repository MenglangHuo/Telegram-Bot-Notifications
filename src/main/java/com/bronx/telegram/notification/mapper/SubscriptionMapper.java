package com.bronx.telegram.notification.mapper;

import com.bronx.telegram.notification.dto.subscription.SubscriptionRequest;
import com.bronx.telegram.notification.dto.subscription.SubscriptionResponse;
import com.bronx.telegram.notification.model.entity.Subscription;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface SubscriptionMapper {

    Subscription toEntity(SubscriptionRequest request);

    void updateEntityFromDto(SubscriptionRequest request, @MappingTarget Subscription subscription);

    @Mapping(source = "partner.id", target = "partnerId")
    @Mapping(source = "partner.partnerName", target = "partnerName")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.organizationName", target = "organizationName")
    @Mapping(source = "division.id", target = "divisionId")
    @Mapping(source = "division.divisionName", target = "divisionName")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.departmentName", target = "departmentName")
    SubscriptionResponse toResponse(Subscription subscription);

    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);

}
