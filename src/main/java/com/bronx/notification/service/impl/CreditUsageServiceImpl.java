package com.bronx.notification.service.impl;

import com.bronx.notification.dto.creditUsage.CreditUsageRequest;
import com.bronx.notification.dto.creditUsage.CreditUsageResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.CreditUsageMapper;
import com.bronx.notification.model.entity.CreditUsage;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.repository.CreditUsageRepository;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.repository.SubscriptionRepository;
import com.bronx.notification.service.CreditUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditUsageServiceImpl implements CreditUsageService {

    private final CreditUsageRepository repository;
    private final CreditUsageMapper mapper;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public CreditUsageResponse create(CreditUsageRequest request) {
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + request.getSubscriptionId()));

        if (subscription.getRemainingCredits() < request.getUsedCredits()) {
            throw new BusinessException("Insufficient credits");
        }

        Notification notification = notificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + request.getSubscriptionId()));

        if (subscription.getRemainingCredits() < request.getUsedCredits()) {
            throw new BusinessException("Insufficient credits");
        }

        CreditUsage entity = mapper.toEntity(request);
        entity.setSubscription(subscription);
        entity.setNotification(notification);
        entity = repository.save(entity);

        // Update subscription remaining credits
        subscription.setRemainingCredits(subscription.getRemainingCredits() - request.getUsedCredits());
        subscriptionRepository.save(subscription);

        return mapper.toResponse(entity);
    }

    @Override
    public List<CreditUsageResponse> findAll() {
        return mapper.toResponses(repository.findAll());
    }
}
