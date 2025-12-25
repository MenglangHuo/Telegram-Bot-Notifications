package com.bronx.notification.controller;


import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.subscriptionHistory.SubscriptionHistoryResponse;
import com.bronx.notification.service.SubscriptionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/subscription-histories")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionHistoryController {

    private final SubscriptionHistoryService subscriptionHistoryService;

    @GetMapping("subscription-id/{id}")
    public ApiResponse<List<SubscriptionHistoryResponse>> listSubscriptionsHistoryBySubscritpionId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @PathVariable Long id,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        List<SubscriptionHistoryResponse> response = subscriptionHistoryService
                .findAll(id,pageable);

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
