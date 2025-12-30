package com.bronx.notification.service.impl;

import com.bronx.notification.dto.creditUsage.CreditUsageRequest;
import com.bronx.notification.dto.creditUsage.CreditUsageResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.CreditUsageMapper;
import com.bronx.notification.model.entity.CreditUsage;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.repository.CreditUsageRepository;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.SubscriptionRepository;
import com.bronx.notification.service.CreditUsageService;
import com.bronx.notification.service.impl.creditMessage.CreditService;
import com.bronx.notification.service.impl.creditMessage.CreditServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
/**
 * Service for managing credit usage records.
 * Note: In REDIS_BATCH mode, most credit usage records are created via batch
 * sync.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditUsageServiceImpl implements CreditUsageService {
    private final CreditUsageRepository repository;
    private final CreditUsageMapper mapper;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final CreditServiceFactory creditServiceFactory;

    @Override
    @Transactional
    public CreditUsageResponse create(CreditUsageRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));
        // Use CreditService for credit validation and deduction
        CreditService creditService = creditServiceFactory.getCreditService();
        if (!creditService.hasCredits(subscription.getId(), request.getUsedCredits())) {
            long current = creditService.getCurrentCredits(subscription.getId());
            throw new BusinessException(String.format(
                    "Insufficient credits: requested %d, available %d",
                    request.getUsedCredits(), current));
        }
        CreditUsage entity = mapper.toEntity(request);
        entity.setSubscription(subscription);
        entity.setUsedAt(Instant.now());
        if (request.getNotificationId() != null) {
            notificationRepository.findById(request.getNotificationId())
                    .ifPresent(entity::setNotification);
        }
        entity = repository.save(entity);
        // Deduct credits using the service (this will use Redis or DB based on config)
        creditService.checkAndDecrementCredit(
                subscription.getId(),
                request.getUsedCredits(),
                request.getNotificationId());
        return mapper.toResponse(entity);
    }
    @Override
    public List<CreditUsageResponse> findAll() {
        return mapper.toResponses(repository.findAll());
    }
    @Override
    public List<CreditUsageResponse> findBySubscriptionId(Long subscriptionId) {
        return mapper.toResponses(repository.findBySubscriptionId(subscriptionId));
    }
    @Override
    public Long getTotalUsedCredits(Long subscriptionId) {
        Long total = repository.sumUsedCreditsBySubscriptionId(subscriptionId);
        return total != null ? total : 0L;
    }
}
