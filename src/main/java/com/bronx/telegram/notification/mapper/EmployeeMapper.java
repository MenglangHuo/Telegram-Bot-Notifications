package com.bronx.telegram.notification.mapper;


import com.bronx.telegram.notification.dto.employee.EmployeeRequest;
import com.bronx.telegram.notification.dto.employee.EmployeeResponse;
import com.bronx.telegram.notification.model.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PartnerMapper.class,
                OrganizationMapper.class,
                DivisionMapper.class,
                DepartmentMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface EmployeeMapper {

    Employee toEntity(Employee employee);

    EmployeeResponse toResponse(Employee employee);

    void updateEmployee(EmployeeRequest request,@MappingTarget Employee employee);
}
