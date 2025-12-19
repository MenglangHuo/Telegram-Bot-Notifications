package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.department.DepartmentRequest;
import com.bronx.telegram.notification.dto.department.DepartmentResponse;
import com.bronx.telegram.notification.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@Slf4j
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request
    ) {
        log.info("Creating department: {}", request.getDepartmentCode());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ApiResponse.success("Department created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getDepartment(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<DepartmentResponse> getDepartmentDetail(
            @PathVariable Long id
    ) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ApiResponse.success(response);
    }


    @GetMapping("/organization/{organizationId}")
    public ApiResponse<PageResponse<DepartmentResponse>> listDepartmentsByOrganization(
            @PathVariable Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DepartmentResponse> response = departmentService
                .getAllDepartmentsByOrganization(organizationId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/division/{divisionId}")
    public ApiResponse<PageResponse<DepartmentResponse>> listDepartmentsByDivision(
            @PathVariable Long divisionId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "query") String query,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DepartmentResponse> response = departmentService
                .getAllDepartmentByDivision(divisionId,query, pageable);
        return ApiResponse.success(response);
    }

//    @GetMapping("/organization/{organizationId}/search")
//    public ApiResponse<PageResponse<DepartmentResponse>> searchDepartmentsByOrganization(
//            @PathVariable Long organizationId,
//            @RequestParam String query,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        PageResponse<DepartmentResponse> response = departmentService
//                .getAllDepartmentsByOrganization(organizationId, query, pageable);
//        return ApiResponse.success(response);
//    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        log.info("Updating department: {}", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ApiResponse.success("Department updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success("Department deleted successfully", null);
    }
}
