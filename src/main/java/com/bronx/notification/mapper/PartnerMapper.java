package com.bronx.notification.mapper;

import com.bronx.notification.dto.partner.PartnerRequest;
import com.bronx.notification.dto.partner.PartnerResponse;
import com.bronx.notification.model.entity.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PartnerMapper {


    Partner toEntity(PartnerRequest request);

    void updateEntity(PartnerRequest request, @MappingTarget Partner partner);

    PartnerResponse toResponse(Partner partner);

    List<PartnerResponse> toResponseList(List<Partner> partners);
}
