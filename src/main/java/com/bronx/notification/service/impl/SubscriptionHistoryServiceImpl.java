package com.bronx.notification.service.impl;

import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryResponse;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.SubscriptionHistoryMapper;
import com.bronx.notification.model.entity.SubscriptionHistory;
import com.bronx.notification.repository.SubscriptionHistoryRepository;
import com.bronx.notification.service.SubscriptionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionHistoryServiceImpl implements SubscriptionHistoryService {
    private final SubscriptionHistoryRepository repository;
    private final SubscriptionHistoryMapper mapper;

    @Override
    public void delete(Long id) {
        SubscriptionHistory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription history not found with id: " + id));
        repository.delete(entity);
    }

    @Override
    public SubscriptionHistoryResponse findById(Long id) {
        SubscriptionHistory entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription history not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<SubscriptionHistoryResponse> findAll() {
        return mapper.toResponses(repository.findAll());
    }
}
