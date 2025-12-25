package com.bronx.notification.service;

import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanRequest;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse create(SubscriptionPlanRequest request);
    SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request);
    void delete(Long id);
    SubscriptionPlanResponse findById(Long id);
    List<SubscriptionPlanResponse> findAll(Pageable pageable, String search);
}
