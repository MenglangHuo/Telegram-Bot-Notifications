package com.bronx.notification.service.impl;

import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanRequest;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.exceptions.DuplicateResourceException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.SubscriptionPlanMapper;
import com.bronx.notification.model.entity.SubscriptionPlan;
import com.bronx.notification.repository.SubscriptionPlanRepository;
import com.bronx.notification.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository repository;
    private final SubscriptionPlanMapper mapper;

    @Override
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            throw new DuplicateResourceException("Subscription plan with code " + request.code() + " already exists");
        }
        SubscriptionPlan entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request) {
        SubscriptionPlan entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + id));

        if (!entity.getCode().equals(request.code()) && repository.findByCode(request.code()).isPresent()) {
            throw new DuplicateResourceException("Subscription plan with code " + request.code() + " already exists");
        }

        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        SubscriptionPlan entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + id));
        repository.delete(entity); // Assuming hard delete; adjust for soft delete if needed
    }

    @Override
    public SubscriptionPlanResponse findById(Long id) {
        SubscriptionPlan entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<SubscriptionPlanResponse> findAll() {
        return mapper.toResponses(repository.findAll());
    }
}
