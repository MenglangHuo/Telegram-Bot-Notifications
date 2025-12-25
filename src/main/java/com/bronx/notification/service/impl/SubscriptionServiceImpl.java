package com.bronx.notification.service.impl;

import com.bronx.notification.dto.subscription.SubscriptionRequest;
import com.bronx.notification.dto.subscription.SubscriptionResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.DuplicateResourceException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.SubscriptionMapper;
import com.bronx.notification.model.entity.OrganizationUnit;
import com.bronx.notification.model.entity.Subscription;
import com.bronx.notification.model.entity.SubscriptionHistory;
import com.bronx.notification.model.entity.SubscriptionPlan;
import com.bronx.notification.model.enumz.SubscriptionAction;
import com.bronx.notification.model.enumz.SubscriptionStatus;
import com.bronx.notification.repository.OrganizationUnitRepository;
import com.bronx.notification.repository.SubscriptionHistoryRepository;
import com.bronx.notification.repository.SubscriptionPlanRepository;
import com.bronx.notification.repository.SubscriptionRepository;
import com.bronx.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final OrganizationUnitRepository orgUnitRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final SubscriptionMapper mapper;

    @Override
    public SubscriptionResponse create(SubscriptionRequest request) {
        OrganizationUnit scope = orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization unit not found with id: " + request.orgUnitId()));
        SubscriptionPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + request.planId()));

        // Example business rule: Check if scope already has an active subscription
         if (subscriptionRepository.findByScopeIdAndSubscriptionId(scope.getId(),plan.getId()).isPresent()) {
             throw new DuplicateResourceException("Scope with this Plan already Exist already has an active subscription");
         }

        Subscription entity = mapper.toEntity(request);
        entity.setScope(scope);
        entity.setPlan(plan);
        entity.setRemainingCredits(plan.getNotificationsCredit());

        //validate durations
        int durationMonths=plan.getDurationMonths();
        if(!isValidPeriod(request.startDate().toString(),request.endDate().toString(),durationMonths)){
            throw new BusinessException("Invalid start date and end date with Plan");
        }

        entity = subscriptionRepository.save(entity);
        saveHistory(entity, plan, SubscriptionAction.NEW);
        return mapper.toResponse(entity);
    }

    @Override
    public SubscriptionResponse update(Long id, SubscriptionRequest request) {
        Subscription entity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));

        OrganizationUnit scope = request.orgUnitId() != null ? orgUnitRepository.findById(request.orgUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization unit not found")) : entity.getScope();

        SubscriptionPlan plan = request.planId() != null ? planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found")) : entity.getPlan();

        mapper.updateEntityFromRequest(request, entity);
        entity.setScope(scope);
        entity.setPlan(plan);
        entity = subscriptionRepository.save(entity);
        saveHistory(entity, plan,request.subscriptionAction());
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        Subscription entity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        subscriptionRepository.delete(entity);
    }

    @Override
    public SubscriptionResponse findById(Long id) {
        Subscription entity = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public List<SubscriptionResponse> findAll(Long scopeId, Pageable pageable) {
        OrganizationUnit scope=null;
        if(scopeId!=null){
            scope = orgUnitRepository.findById(scopeId).orElse(null);
        }

        return mapper.toResponses(subscriptionRepository.findActiveByScope(scope,pageable));
    }

    private void saveHistory(Subscription sub, SubscriptionPlan plan, SubscriptionAction action) {
        SubscriptionHistory history = new SubscriptionHistory();
        history.setSubscription(sub);
        history.setPlan(plan);
        history.setStartDate(sub.getStartDate());
        history.setEndDate(sub.getEndDate());
        history.setGrantedCredits(plan.getNotificationsCredit());
        history.setAction(action);
        historyRepository.save(history);
    }


    public boolean isValidPeriod(String startIso, String endIso, int minMonths) {
        try {
            if (startIso == null || endIso == null) return false;

            ZonedDateTime start = ZonedDateTime.parse(startIso);
            ZonedDateTime end = ZonedDateTime.parse(endIso);

            // 1. Strict check: startDate must be less than endDate
            if (!start.isBefore(end)) {
                throw new BusinessException("Start date is Bigger Than End Date!");
            }

            // 2. Duration check: end must be >= (start + minMonths)
            ZonedDateTime requiredThreshold = start.plusMonths(minMonths);

            if (end.isAfter(requiredThreshold)) {
                log.warn("Validation failed: Duration is greater than {} months", minMonths);
                return false;
            }
            return true;

        } catch (DateTimeParseException e) {
            log.error("Invalid date format provided: start={}, end={}", startIso, endIso);
            return false;
        }
    }
}
