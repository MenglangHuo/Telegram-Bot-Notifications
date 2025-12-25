package com.bronx.notification.service;

import com.bronx.notification.dto.subscription.SubscriptionRequest;
import com.bronx.notification.dto.subscription.SubscriptionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse create(SubscriptionRequest request);
    SubscriptionResponse update(Long id, SubscriptionRequest request);
    void delete(Long id);
    SubscriptionResponse findById(Long id);
    List<SubscriptionResponse> findAll(Long scopeId, Pageable pageable);
}
