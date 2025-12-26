package com.bronx.notification.controller;

import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanRequest;
import com.bronx.notification.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.bronx.notification.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionPlanResponse> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {

        SubscriptionPlanResponse response = subscriptionPlanService.create(request);
        return ApiResponse.success("Subscription created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionPlanResponse> getSubscription(@PathVariable Long id) {
        SubscriptionPlanResponse response = subscriptionPlanService.findById(id);
        return ApiResponse.success("Subscription Plan created successfully", response);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<SubscriptionPlanResponse> getSubscriptionDetail(
            @PathVariable Long id
    ) {
        SubscriptionPlanResponse response = subscriptionPlanService
                .findById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<SubscriptionPlanResponse>> listSubscriptionPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "search",required=false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        List<SubscriptionPlanResponse> response = subscriptionPlanService
                .findAll(pageable,search);
       return ApiResponse.success(response);
    }

}
