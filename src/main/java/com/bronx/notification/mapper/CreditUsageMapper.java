package com.bronx.notification.mapper;

import com.bronx.notification.dto.creditUsage.CreditUsageRequest;
import com.bronx.notification.dto.creditUsage.CreditUsageResponse;
import com.bronx.notification.model.entity.CreditUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {PartnerMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface CreditUsageMapper {

    CreditUsage toEntity(CreditUsageRequest request);
    void updateEntityFromRequest(CreditUsageRequest request, @MappingTarget CreditUsage entity);
    CreditUsageResponse toResponse(CreditUsage entity);
    List<CreditUsageResponse> toResponses(List<CreditUsage> entities);
}
