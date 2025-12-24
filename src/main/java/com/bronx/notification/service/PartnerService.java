package com.bronx.notification.service;

import com.bronx.notification.dto.baseResponse.PageResponse;
import com.bronx.notification.dto.partner.PartnerRequest;
import com.bronx.notification.dto.partner.PartnerResponse;
import org.springframework.data.domain.Pageable;

public interface PartnerService {
    PartnerResponse createPartner(PartnerRequest request);
    PartnerResponse updatePartner(Long id, PartnerRequest request);
    void deletePartner(Long id);
    PageResponse<PartnerResponse> listPartners(Pageable pageable);
    PageResponse<PartnerResponse> listPartnersByStatus(
            boolean status,
            Pageable pageable
    );
    PageResponse<PartnerResponse> searchPartners(String search, Pageable pageable);
    PartnerResponse regenerateSecretKey(Long id);
    PartnerResponse activatePartner(Long id);
    PartnerResponse getPartnerByClientIdAndSecretKey(String clientId, String secretKey);
    PartnerResponse getPartner(Long id);

}
