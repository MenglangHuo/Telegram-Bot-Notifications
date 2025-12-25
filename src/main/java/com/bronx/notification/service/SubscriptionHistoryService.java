package com.bronx.notification.service;

import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryRequest;
import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionHistoryService {
    void delete(Long id);
    SubscriptionHistoryResponse findById(Long id);
    List<SubscriptionHistoryResponse> findAll(Long subscriptionId, Pageable pageable);
}
