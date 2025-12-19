package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.division.DivisionRequest;
import com.bronx.telegram.notification.dto.division.DivisionResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.DuplicateResourceException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.DivisionMapper;
import com.bronx.telegram.notification.model.entity.Division;
import com.bronx.telegram.notification.model.entity.Organization;
import com.bronx.telegram.notification.repository.DivisionRepository;
import com.bronx.telegram.notification.repository.OrganizationRepository;
import com.bronx.telegram.notification.service.DivisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final OrganizationRepository organizationRepository;
    private final DivisionMapper divisionMapper;

    @Override
    public DivisionResponse createDivision(DivisionRequest request) {
        // Validate organization exists and has hierarchy enabled
        Organization organization = findOrganizationById(request.getOrganizationId());

        if (!organization.getHierarchyEnabled()) {
            throw new BusinessException(
                    "Organization does not have hierarchy enabled"
            );
        }

        // Validate uniqueness within organization
        if (divisionRepository.existsByOrganizationIdAndDivisionCodeAndDivisionName(organization.getId(), request.getDivisionCode(), request.getDivisionName())) {
            throw new DuplicateResourceException(
                    "Division with code " + request.getDivisionCode() +
                            " already exists in this organization"
            );
        }

        // Map and save
        Division division = divisionMapper.toEntity(request);
        division.setOrganization(organization);

        Division saved = divisionRepository.save(division);
        log.info("Division created successfully with ID: {}", saved.getId());

        return divisionMapper.toResponse(saved);
    }

    @Override
    public DivisionResponse updateDivision(Long id, DivisionRequest request) {
        Division division = findDivisionById(id);
        divisionMapper.updateEntityFromDto(request, division);

        Division updated = divisionRepository.save(division);
        log.info("Division updated successfully: {}", id);

        return divisionMapper.toResponse(updated);
    }

    @Override
    public void deleteDivision(Long id) {
        Division division = findDivisionById(id);
        division.setDeletedAt(Instant.now());
        division.setIsActive(false);
        divisionRepository.save(division);
    }

    @Override
    public DivisionResponse getDivisionById(Long id) {
        Division division = findDivisionById(id);
        DivisionResponse response = divisionMapper.toResponse(division);

//        // Add statistics
//        response.setDepartmentCount(
//                divisionRepository.countDepartmentsByDivisionId(id)
//        );
//        response.setEmployeeCount(
//                divisionRepository.countEmployeesByDivisionId(id)
//        );
//        response.setActiveSubscriptionCount(
//                divisionRepository.countActiveSubscriptionsByDivisionId(id)
//        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> listDivisions(Pageable pageable) {
        return null;
    }

    @Override
    public PageResponse<DivisionResponse> listDivisionsByOrganization(Long organizationId, Pageable pageable) {
        return null;
    }

    @Override
    public PageResponse<DivisionResponse> searchDivisionsByOrganization(Long organizationId, String search, Pageable pageable) {
        Page<Division> divisions = divisionRepository
                .searchDivisionsByOrganization(organizationId, search, pageable);
        Page<DivisionResponse> responsePage = divisions.map(divisionMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    // Helper methods

    private Division findDivisionById(Long id) {
        return divisionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Division not found with ID: " + id
                ));
    }

    private Organization findOrganizationById(Long id) {
        return organizationRepository.findOrganizationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with ID: " + id
                ));
    }
}

