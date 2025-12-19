package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.subscription.SubscriptionRequest;
import com.bronx.telegram.notification.dto.subscription.SubscriptionResponse;
import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.model.entity.Subscription;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(SubscriptionRequest request);
    SubscriptionResponse getSubscription(Long id);
    PageResponse<SubscriptionResponse> listSubscriptionsByPartner(
            Long partnerId,
            Pageable pageable
    );
    SubscriptionResponse updateSubscription(
            Long id,
            SubscriptionRequest request
    );
    SubscriptionResponse activateSubscription(Long id);
    SubscriptionResponse cancelSubscription(Long id);
    void deleteSubscription(Long id);
    void checkExpiringSubscriptions();
    PageResponse<SubscriptionResponse> listSubscriptions(Pageable pageable);
    PageResponse<SubscriptionResponse> listSubscriptionsByPartnerAndStatus(
            Long partnerId,
            SubscriptionStatus status,
            Pageable pageable
    );
    Subscription getSubscriptionForEmployee(Employee employee);
}
