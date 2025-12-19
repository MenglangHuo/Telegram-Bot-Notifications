package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.organization.OrganizationRequest;
import com.bronx.telegram.notification.dto.organization.OrganizationResponse;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {

    OrganizationResponse createOrganization(OrganizationRequest request);
    OrganizationResponse getOrganization(Long id);
    PageResponse<OrganizationResponse> listOrganizations(Pageable pageable);
    PageResponse<OrganizationResponse> listOrganizationsByPartner(
            Long partnerId,
            Pageable pageable
    );
    PageResponse<OrganizationResponse> listOrganizationsByPartnerAndStatus(
            Long partnerId,
            Pageable pageable
    );
    PageResponse<OrganizationResponse> searchOrganizationsByPartner(
            Long partnerId,
            String search,
            Pageable pageable
    );
    OrganizationResponse updateOrganization(
            Long id,
            OrganizationRequest request
    );
    void deleteOrganization(Long id);
}
