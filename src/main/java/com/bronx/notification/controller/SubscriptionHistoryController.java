package com.bronx.notification.controller;


import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.subscription.SubscriptionRequest;
import com.bronx.notification.dto.subscription.SubscriptionResponse;
import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryResponse;
import com.bronx.notification.service.SubscriptionHistoryService;
import com.bronx.notification.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscription-histories")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionHistoryController {

    private final SubscriptionHistoryService subscriptionHistoryService;

    @GetMapping("find-by-subscription-id")
    public ApiResponse<List<SubscriptionHistoryResponse>> listSubscriptionsHistoryBySubscritpionId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name="subscriptionId") Long subscriptionId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        List<SubscriptionHistoryResponse> response = subscriptionHistoryService
                .findAll(subscriptionId,pageable);

        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionHistoryResponse> findById(
            @PathVariable Long id
    ) {
        SubscriptionHistoryResponse response = subscriptionHistoryService
                .findById(id);
        return ApiResponse.success(response);
    }



}
