package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.subscription.SubscriptionRequest;
import com.bronx.telegram.notification.dto.subscription.SubscriptionResponse;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.SubscriptionMapper;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import com.bronx.telegram.notification.repository.*;
import com.bronx.telegram.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PartnerRepository partnerRepository;
    private final CompanyRepository companyRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final SubscriptionMapper subscriptionMapper;


    @Override
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        // Validate partner
        Partner partner = findActivePartner(request.getPartnerId());
        Company company = null;
        OrganizationUnit scope = null;
        if (request.getCompanyId() == null) {
            throw new BusinessException("Company ID is required");
        }
        company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        validateCompanyBelongsToPartner(company, partner);

        if (request.getOrgUnitId() != null) {
            scope = organizationUnitRepository.findById(request.getOrgUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrganizationUnit not found"));
            validateOrgUnitBelongsToCompany(scope, company);
        }

        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setPartner(partner);
        subscription.setCompany(company);
        subscription.setScope(scope);
        // Set subscriptionType based on scope (e.g., if scope==null -> COMPANY, else UNIT)
        Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Long id) {
        Subscription subscription = findSubscriptionById(id);
        return subscriptionMapper.toResponse(subscription);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubscriptionResponse> listSubscriptionsByPartner(
            Long partnerId,
            Pageable pageable
    ) {
        Page<Subscription> subscriptions = subscriptionRepository
                .findAllByPartnerId(partnerId,pageable);
        Page<SubscriptionResponse> responsePage = subscriptions
                .map(subscriptionMapper::toResponse);
        return PageResponse.of(responsePage);
    }



    @Override
    public SubscriptionResponse activateSubscription(Long id) {
        Subscription subscription = findSubscriptionById(id);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription updated = subscriptionRepository.save(subscription);
        log.info("Subscription activated: {}", id);

        return subscriptionMapper.toResponse(updated);
    }

    @Override
    public SubscriptionResponse cancelSubscription(Long id) {
        Subscription subscription = findSubscriptionById(id);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(Instant.now());

        Subscription updated = subscriptionRepository.save(subscription);
        log.info("Subscription cancelled: {}", id);

        return subscriptionMapper.toResponse(updated);
    }

    private void validateCompanyBelongsToPartner(Company company, Partner partner) {
        if (!company.getPartner().getId().equals(partner.getId())) {
            throw new BusinessException("Company does not belong to the specified partner");
        }
    }

    private void validateOrgUnitBelongsToCompany(OrganizationUnit unit, Company company) {
        if (!unit.getCompany().getId().equals(company.getId())) {
            throw new BusinessException("OrganizationUnit does not belong to the company");
        }
    }

    @Override
    public void deleteSubscription(Long id) {
        Subscription subscription = findSubscriptionById(id);

        // Check for active bots
//        Integer botCount = subscriptionRepository.countBotsBySubscriptionId(id);
//        if (botCount > 0) {
//            throw new BusinessException(
//                    "Cannot delete subscription with " + botCount + " active bots"
//            );
//        }

        subscription.setDeletedAt(Instant.now());
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        log.info("Subscription deleted successfully: {}", id);
    }

    //TODO cron job
    @Override
    public void checkExpiringSubscriptions() {
//        Instant oneWeekFromNow = Instant.now().plus(7, ChronoUnit.DAYS);
//        List<Subscription> expiring = subscriptionRepository
//                .findExpiringSubscriptions(oneWeekFromNow);
//
//        log.info("Found {} subscriptions expiring within 7 days", expiring.size());
//
//        // Here you would send notifications to partners about expiring subscriptions
//        for (Subscription subscription : expiring) {
//            log.warn("Subscription {} expires at {}",
//                    subscription.getId(), subscription.getEndDate());
//            // TODO: Send notification
//        }
    }

    @Override
    public PageResponse<SubscriptionResponse> listSubscriptions(Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository
                .findAll(pageable);
        Page<SubscriptionResponse> responsePage = subscriptions
                .map(subscriptionMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public PageResponse<SubscriptionResponse> listSubscriptionsByPartnerAndStatus(Long partnerId, SubscriptionStatus status, Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionRepository
                .findAllByPartnerIdAndStatus(partnerId, status,pageable);
        Page<SubscriptionResponse> responsePage = subscriptions
                .map(subscriptionMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    public Subscription getSubscriptionForEmployee(Employee employee) {
        OrganizationUnit currentUnit = employee.getOrganizationUnit();

        while (currentUnit != null) {
            Optional<Subscription> subscription = subscriptionRepository
                    .findActiveSubscriptionByOrgUnit(currentUnit.getId());

            if (subscription.isPresent()) {
                return subscription.get();
            }

            // Move up to parent unit
            currentUnit = currentUnit.getParent();
        }

        // Fall back to company-wide subscription
        return subscriptionRepository
                .findActiveCompanySubscription(employee.getCompany().getId())
                .orElse(null);

    }


    // Helper methods
    private Subscription findSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with ID: " + id
                ));
    }

    private Partner findActivePartner(Long partnerId) {
        Partner partner = partnerRepository.findByIdAndDeletedAtIsNull(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partner not found with ID: " + partnerId
                ));

        if (partner.getIsActive().equals(Boolean.FALSE)) {
            throw new BusinessException(
                    "Partner is not active. Status: " + partner.getStatus()
            );
        }
        return partner;
    }
    @Override
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        Subscription subscription = findSubscriptionById(id);

        //TODO
        // Validate if reducing limits
//        if (request.getMaxTelegramBots() != null &&
//                request.getMaxTelegramBots() < subscription.getMaxTelegramBots()) {
//            Integer currentBots = subscriptionRepository
//                    .countBotsBySubscriptionId(id);

//            if (currentBots > request.getMaxTelegramBots()) {
//                throw new BusinessException(
//                        String.format(
//                                "Cannot reduce max bots to %d. Current count: %d",
//                                request.getMaxTelegramBots(), currentBots
//                        )
//                );
//            }
//    }
        subscriptionMapper.updateEntityFromDto(request, subscription);
        Subscription updated = subscriptionRepository.save(subscription);
        log.info("Subscription updated successfully: {}", id);
        return subscriptionMapper.toResponse(updated);

    }

//    private void validateOrganizationBelongsToPartner(
//            Organization organization,
//            Partner partner
//    ) {
//        if (!organization.getPartner().getId().equals(partner.getId())) {
//            throw new BusinessException(
//                    "Organization does not belong to the specified partner"
//            );
//        }
//    }
}
