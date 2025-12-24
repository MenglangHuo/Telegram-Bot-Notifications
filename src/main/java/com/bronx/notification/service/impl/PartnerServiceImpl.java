package com.bronx.notification.service.impl;

import com.bronx.notification.dto.baseResponse.PageResponse;
import com.bronx.notification.dto.partner.PartnerRequest;
import com.bronx.notification.dto.partner.PartnerResponse;
import com.bronx.notification.exceptions.DuplicateResourceException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.PartnerMapper;
import com.bronx.notification.model.entity.Partner;
import com.bronx.notification.repository.PartnerRepository;
import com.bronx.notification.service.PartnerService;
import com.bronx.notification.utils.EncryptionUtils;
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
    private final PartnerMapper partnerMapper;

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
         partner.setSecretKey(EncryptionUtils.encrypt(generateSecretKey()));

        // Set defaults if not provided

        // Save
        Partner saved = partnerRepository.save(partner);
        log.info("Partner created successfully with ID: {}", partner.getSecretKey());
        saved.setSecretKey(EncryptionUtils.decrypt(saved.getSecretKey()));

        return partnerMapper.toResponse(saved);
    }

    @Override
    public PartnerResponse updatePartner(Long id, PartnerRequest request) {
        Partner partner = findPartnerById(id);

        // Update fields
        partnerMapper.updateEntity(request, partner);

        Partner updated = partnerRepository.save(partner);
        log.info("Partner updated successfully: {}", id);

        return partnerMapper.toResponse(updated);
    }

    @Override
    public void deletePartner(Long id) {
        Partner partner = findPartnerById(id);

        partner.setDeletedAt(Instant.now());
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

}
