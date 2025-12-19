package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.partner.PartnerRequest;
import com.bronx.telegram.notification.dto.partner.PartnerResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.DuplicateResourceException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.PartnerMapper;
import com.bronx.telegram.notification.model.entity.Partner;
import com.bronx.telegram.notification.model.enumz.SubscriptionTier;
import com.bronx.telegram.notification.repository.OrganizationRepository;
import com.bronx.telegram.notification.repository.PartnerRepository;
import com.bronx.telegram.notification.service.AuthenticationService;
import com.bronx.telegram.notification.service.PartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final OrganizationRepository organizationRepository;
    private final PartnerMapper partnerMapper;
    private final EncryptionService encryptionService;

    @Value("${app.partner.default-max-organizations:5}")
    private Integer defaultMaxOrganizations;

    @Value("${app.partner.default-max-bots:10}")
    private Integer defaultMaxBots;

    @Override
    public PartnerResponse createPartner(PartnerRequest request) {
        // Validate uniqueness
        if (partnerRepository.existsByPartnerCodeAndDeletedAtIsNull((request.getPartnerCode()))){
            throw new DuplicateResourceException(
                    "Partner with code " + request.getPartnerCode() + " already exists"
            );
        }


        // Map to entity
        Partner partner = partnerMapper.toEntity(request);

        // Generate client credentials
         partner.setSecretKey(encryptionService.encrypt(generateSecretKey()));

        // Set defaults if not provided
        if (partner.getMaxOrganizations() == null) {
            partner.setMaxOrganizations(getDefaultMaxOrganizations(request.getSubscriptionTier()));
        }
        if (partner.getMaxBots() == null) {
            partner.setMaxBots(getDefaultMaxBots(request.getSubscriptionTier()));
        }

        // Save
        Partner saved = partnerRepository.save(partner);
        log.info("Partner created successfully with ID: {}", partner.getSecretKey());
        saved.setSecretKey(encryptionService.decrypt(saved.getSecretKey()));

        return partnerMapper.toResponse(saved);
    }

    @Override
    public PartnerResponse updatePartner(Long id, PartnerRequest request) {
        Partner partner = findPartnerById(id);
        // Validate limits if being reduced
        if (request.getMaxOrganizations() != null &&
                request.getMaxOrganizations() < partner.getMaxOrganizations()) {

            Integer currentCount = organizationRepository
                    .countOrganizationsByPartnerId(id);

            if (currentCount > request.getMaxOrganizations()) {
                throw new BusinessException(
                        String.format(
                                "Cannot reduce max organizations to %d. Current count: %d",
                                request.getMaxOrganizations(), currentCount
                        )
                );
            }
        }

        // Update fields
        partnerMapper.updateEntity(request, partner);

        Partner updated = partnerRepository.save(partner);
        log.info("Partner updated successfully: {}", id);

        return partnerMapper.toResponse(updated);
    }

    @Override
    public void deletePartner(Long id) {
        Partner partner = findPartnerById(id);

        // Check for active organizations
        Integer orgCount = organizationRepository.countOrganizationsByPartnerId(id);
        if (orgCount > 0) {
            throw new BusinessException(
                    "Cannot delete partner with " + orgCount + " active organizations"
            );
        }

        partner.setDeletedAt(Instant.now());
        partner.setIsActive(false);
        partnerRepository.save(partner);

        log.info("Partner deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnerResponse> listPartners(Pageable pageable) {
        Page<Partner> partners = partnerRepository.findAllByDeletedAtIsNull(pageable);
        Page<PartnerResponse> responsePage = partners.map(partnerMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<PartnerResponse> listPartnersByStatus(boolean status, Pageable pageable) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PartnerResponse> searchPartners(String search, Pageable pageable) {
        Page<Partner> partners = partnerRepository.searchPartners(search, pageable);
        Page<PartnerResponse> responsePage = partners.map(partnerMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PartnerResponse regenerateSecretKey(Long id) {
        return null;
    }

    @Override
    public PartnerResponse activatePartner(Long id) {
        return null;
    }


    @Transactional(readOnly = true)
    @Override
    public PartnerResponse getPartnerByClientIdAndSecretKey(String clientId, String secretKey) {
        Partner partner = partnerRepository.findByClientIdAndSecretKey(clientId,secretKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with client ID: " + clientId
                ));
        return partnerMapper.toResponse(partner);
    }

    @Override
    public PartnerResponse getPartner(Long id) {
        Partner partner = findPartnerById(id);
        PartnerResponse response = partnerMapper.toResponse(partner);

        // Add statistics
        response.setCurrentOrganizationCount(organizationRepository.countOrganizationsByPartnerId((id)));
        response.setCurrentBotCount(
                partnerRepository.countTelegramBotById((id)
        ));
        response.setTotalSubscriptions(
                partnerRepository.countSubscriptionsById((id))
        );

        return response;
    }


    // Helper methods
    private Partner findPartnerById(Long id) {
        return partnerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with ID: " + id
                ));
    }

    private String generateSecretKey() {
        // In production, use proper encryption
        return  UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private Integer getDefaultMaxOrganizations(SubscriptionTier tier) {
        return switch (tier) {
            case BASIC -> 1;
            case PREMIUM -> 5;
            case ENTERPRISE -> 50;
        };
    }

    private Integer getDefaultMaxBots(SubscriptionTier tier) {
        return switch (tier) {
            case BASIC -> 5;
            case PREMIUM -> 20;
            case ENTERPRISE -> 100;
        };
    }
}
