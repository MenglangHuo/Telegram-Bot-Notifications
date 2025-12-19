package com.bronx.telegram.notification.mapper;

import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitRequest;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitResponse;
import com.bronx.telegram.notification.dto.organizationUnit.OrganizationUnitTreeNode;
import com.bronx.telegram.notification.model.entity.OrganizationUnit;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationUnitMapper {

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "company", ignore = true)
//    @Mapping(target = "parent", ignore = true)
//    @Mapping(target = "children", ignore = true)
//    @Mapping(target = "path", ignore = true)
//    @Mapping(target = "hierarchyLevel", ignore = true)
//    @Mapping(target = "fullPath", ignore = true)
//    @Mapping(target = "depth", ignore = true)
    OrganizationUnit toEntity(OrganizationUnitRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.unitName")
    @Mapping(target = "childrenCount", expression = "java(entity.getChildren() != null ? entity.getChildren().size() : 0)")
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
