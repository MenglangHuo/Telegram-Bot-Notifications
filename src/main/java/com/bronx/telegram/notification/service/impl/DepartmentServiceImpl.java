package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.department.DepartmentRequest;
import com.bronx.telegram.notification.dto.department.DepartmentResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.DuplicateResourceException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.DepartmentMapper;
import com.bronx.telegram.notification.model.entity.Department;
import com.bronx.telegram.notification.model.entity.Division;
import com.bronx.telegram.notification.model.entity.Organization;
import com.bronx.telegram.notification.repository.DepartmentRepository;
import com.bronx.telegram.notification.repository.DivisionRepository;
import com.bronx.telegram.notification.repository.OrganizationRepository;
import com.bronx.telegram.notification.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final DivisionRepository divisionRepository;
    private final DepartmentMapper departmentMapper;


    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        // Validate organization exists
        Organization organization =findOrganizationById(request.getOrganizationId());

        // Validate division if provided
        Division division = null;
        if (request.getDivisionId() != null) {
            division =divisionRepository.findById(request.getDivisionId()).orElseThrow(()-> new ResourceNotFoundException("Not found Division"));

            // Ensure division belongs to the same organization
            if (!division.getOrganization().getId().equals(organization.getId())) {
                throw new BusinessException(
                        "Division does not belong to the specified organization"
                );
            }

            // Ensure organization has hierarchy enabled
            if (Boolean.FALSE.equals(organization.getHierarchyEnabled())) {
                throw new BusinessException(
                        "Organization does not have hierarchy enabled"
                );
            }
        }
        // Validate uniqueness within organization
        if (departmentRepository.existsByOrganizationIdAndDepartmentName(
                organization.getId(), request.getDepartmentName())) {
            throw new DuplicateResourceException(
                    "Department with code " + request.getDepartmentCode() +
                            " already exists in this organization"
            );
        }

        // Map and save
        Department department = departmentMapper.toEntity(request);
        department.setOrganization(organization);
        department.setDivision(division);

        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", saved.getId());

        return departmentMapper.toResponse(saved);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        log.info("Updating department ID: {}", id);

        Department department = findDepartmentById(id);
        departmentMapper.updateEntityFromDto(request, department);

        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully: {}", id);

        return departmentMapper.toResponse(updated);
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        Department department = findDepartmentById(departmentId);
        department.setDeletedAt(Instant.now());
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = findDepartmentById(id);
        DepartmentResponse response = departmentMapper.toResponse(department);

        // Add statistics
//        response.setEmployeeCount(
//                departmentRepository.countEmployeesByDepartmentId(id)
//        );
//        response.setActiveSubscriptionCount(
//                departmentRepository.countActiveSubscriptionsByDepartmentId(id)
//        );

        return response;
    }

    @Override
    public PageResponse<DepartmentResponse> getAllDepartmentsByOrganization(Long organizationId, Long divisionId, Pageable pageable) {
        return null;
    }

    @Override
    public PageResponse<DepartmentResponse> getAllDepartmentByDivision(Long divisionId,String search, Pageable pageable) {
        Page<Department> departments = departmentRepository
                .searchDepartmentsByDivision(divisionId, search, pageable);
        Page<DepartmentResponse> responsePage = departments
                .map(departmentMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<DepartmentResponse> getAllDepartmentsByOrganization(Long organizationId, Pageable pageable) {
        return null;
    }

    // Helper methods

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id
                ));
    }

    private Organization findOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with ID: " + id
                ));
    }

    private Division findDivisionById(Long id) {
        return divisionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Division not found with ID: " + id
                ));
    }
}
