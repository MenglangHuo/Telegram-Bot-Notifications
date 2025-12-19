package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.organization.OrganizationRequest;
import com.bronx.telegram.notification.dto.organization.OrganizationResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.DuplicateResourceException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.OrganizationMapper;
import com.bronx.telegram.notification.model.entity.Organization;
import com.bronx.telegram.notification.model.entity.Partner;
import com.bronx.telegram.notification.repository.DepartmentRepository;
import com.bronx.telegram.notification.repository.DivisionRepository;
import com.bronx.telegram.notification.repository.OrganizationRepository;
import com.bronx.telegram.notification.repository.PartnerRepository;
import com.bronx.telegram.notification.service.OrganizationService;
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
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PartnerRepository partnerRepository;
    private final OrganizationMapper organizationMapper;


    @Override
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        // Validate partner exists and is active
        Partner partner = findActivePartner(request.getPartnerId());

        // Check partner limits
        Integer currentCount = organizationRepository
                .countOrganizationsByPartnerId(partner.getId());

        if (currentCount >= partner.getMaxOrganizations()) {
            throw new BusinessException(
                    String.format(
                            "Partner has reached maximum organizations limit (%d/%d)",
                            currentCount, partner.getMaxOrganizations()
                    )
            );
        }

        // Validate uniqueness within partner
        if (organizationRepository.existsByPartnerIdAndOrganizationCodeAndDeletedAtIsNull(partner.getId(), request.getOrganizationCode(), request.getOrganizationName())) {
            throw new DuplicateResourceException(
                    "Organization with code " + request.getOrganizationCode() +
                            " already exists for this partner"
            );
        }

        // Map and save
        Organization organization = organizationMapper.toEntity(request);
        organization.setPartner(partner);

        Organization saved = organizationRepository.save(organization);
        log.info("Organization created successfully with ID: {}", saved.getId());

        return organizationMapper.toResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganization(Long id) {
        Organization organization = findOrganizationById(id);
        OrganizationResponse response = organizationMapper.toResponse(organization);

        // Add statistics
//        response.setDivisionCount(
//                organizationRepository.countDivisionsByOrganizationId(id)
//        );

//        response.setDepartmentCount(
//                organizationRepository.countDepartmentsByOrganizationId(id)
//        );
//        response.setEmployeeCount(
//                organizationRepository.countEmployeesByOrganizationId(id)
//        );
//        response.setActiveSubscriptionCount(
//                organizationRepository.countActiveSubscriptionsByOrganizationId(id)
//        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> listOrganizations(Pageable pageable) {
        Page<Organization> organizations = organizationRepository
                .findAll(pageable);
        Page<OrganizationResponse> responsePage = organizations
                .map(organizationMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> listOrganizationsByPartner(
            Long partnerId,
            Pageable pageable
    ) {
        Page<Organization> organizations = organizationRepository
                .findAllByPartnerId(partnerId,pageable);
        Page<OrganizationResponse> responsePage = organizations
                .map(organizationMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> listOrganizationsByPartnerAndStatus(
            Long partnerId,
            Pageable pageable
    ) {
        Page<Organization> organizations = organizationRepository
                .findAllByPartnerId(partnerId,pageable);
        Page<OrganizationResponse> responsePage = organizations
                .map(organizationMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<OrganizationResponse> searchOrganizationsByPartner(Long partnerId, String search, Pageable pageable) {
        return null;
    }

    @Override
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        Organization organization = findOrganizationById(id);
        organizationMapper.updateEntityFromDto(request, organization);

        Organization updated = organizationRepository.save(organization);
        log.info("Organization updated successfully: {}", id);

        return organizationMapper.toResponse(updated);
    }

    @Override
    public void deleteOrganization(Long id) {
        Organization organization = findOrganizationById(id);
        organization.setDeletedAt(Instant.now());
        organization.setIsActive(false);
        organizationRepository.save(organization);
    }

    // Helper methods
    private Organization findOrganizationById(Long id) {
        return organizationRepository.findOrganizationById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with ID: " + id
                ));
    }

    private Partner findActivePartner(Long partnerId) {
        Partner partner = partnerRepository.findByIdAndDeletedAtIsNull(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with ID: " + partnerId
                ));

        return partner;
    }
}
