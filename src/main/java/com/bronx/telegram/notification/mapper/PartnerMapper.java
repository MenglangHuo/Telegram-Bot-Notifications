package com.bronx.telegram.notification.mapper;

import com.bronx.telegram.notification.dto.partner.PartnerRequest;
import com.bronx.telegram.notification.dto.partner.PartnerResponse;
import com.bronx.telegram.notification.model.entity.Partner;
import org.mapstruct.*;

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
