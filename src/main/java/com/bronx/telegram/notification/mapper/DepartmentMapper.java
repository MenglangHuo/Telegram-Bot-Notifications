package com.bronx.telegram.notification.mapper;
import com.bronx.telegram.notification.dto.department.DepartmentRequest;
import com.bronx.telegram.notification.dto.department.DepartmentResponse;
import com.bronx.telegram.notification.model.entity.Department;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring",
       unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface DepartmentMapper {

    Department toEntity(DepartmentRequest request);

    void updateEntityFromDto(DepartmentRequest request, @MappingTarget Department department);

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.organizationName", target = "organizationName")
    @Mapping(source = "division.id", target = "divisionId")
    @Mapping(source = "division.divisionName", target = "divisionName")
    DepartmentResponse toResponse(Department department);

    List<DepartmentResponse> toResponseList(List<Department> departments);
}
