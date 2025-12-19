package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.department.DepartmentRequest;
import com.bronx.telegram.notification.dto.department.DepartmentResponse;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest department);
    DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest department);
    void deleteDepartment(Long departmentId);
    DepartmentResponse getDepartmentById(Long departmentId);
    PageResponse<DepartmentResponse> getAllDepartmentsByOrganization(Long organizationId,Long divisionId, Pageable pageable);
    PageResponse<DepartmentResponse> getAllDepartmentByDivision(Long divisionId, String search,Pageable pageable);
    PageResponse<DepartmentResponse> getAllDepartmentsByOrganization(Long organizationId, Pageable pageable);
}
