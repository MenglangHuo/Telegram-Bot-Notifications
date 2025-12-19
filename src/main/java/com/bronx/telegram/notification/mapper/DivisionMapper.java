package com.bronx.telegram.notification.mapper;

import com.bronx.telegram.notification.dto.division.DivisionRequest;
import com.bronx.telegram.notification.dto.division.DivisionResponse;
import com.bronx.telegram.notification.model.entity.Division;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface DivisionMapper {

    Division toEntity(DivisionRequest request);

    void updateEntityFromDto(DivisionRequest request, @MappingTarget Division division);

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.organizationName", target = "organizationName")
    DivisionResponse toResponse(Division division);

    List<DivisionResponse> toResponseList(List<Division> divisions);
}
