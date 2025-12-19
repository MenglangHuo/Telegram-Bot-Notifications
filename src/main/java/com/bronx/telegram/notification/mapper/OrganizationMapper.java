package com.bronx.telegram.notification.mapper;
import com.bronx.telegram.notification.dto.organization.OrganizationRequest;
import com.bronx.telegram.notification.dto.organization.OrganizationResponse;
import com.bronx.telegram.notification.model.entity.Organization;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PartnerMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    Organization toEntity(OrganizationRequest request);

    void updateEntityFromDto(OrganizationRequest request, @MappingTarget Organization organization);

    @Mapping(source = "partner.id", target = "partnerId")
    @Mapping(source = "partner.partnerName", target = "partnerName")
    OrganizationResponse toResponse(Organization organization);

    List<OrganizationResponse> toResponseList(List<Organization> organizations);
}
