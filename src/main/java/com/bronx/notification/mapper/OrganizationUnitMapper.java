package com.bronx.notification.mapper;

import com.bronx.notification.dto.organizationUnit.OrganizationUnitRequest;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitResponse;
import com.bronx.notification.dto.organizationUnit.OrganizationUnitTreeNode;
import com.bronx.notification.model.entity.OrganizationUnit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationUnitMapper {


    OrganizationUnit toEntity(OrganizationUnitRequest request);


    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    OrganizationUnitResponse toResponse(OrganizationUnit entity);

    List<OrganizationUnitResponse> toResponseList(List<OrganizationUnit> entities);

    @Mapping(target = "hasChildren", expression = "java(entity.hasChildren())")
    @Mapping(target = "children", ignore = true)
    OrganizationUnitTreeNode toTreeNode(OrganizationUnit entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "hierarchyLevel", ignore = true)
    @Mapping(target = "fullPath", ignore = true)
    void updateEntityFromRequest(OrganizationUnitRequest request, @MappingTarget OrganizationUnit entity);
}
