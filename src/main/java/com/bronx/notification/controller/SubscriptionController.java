package com.bronx.notification.controller;


import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.baseResponse.PageResponse;
import com.bronx.notification.dto.subscription.SubscriptionRequest;
import com.bronx.notification.dto.subscription.SubscriptionResponse;
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

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request
    ) {

        SubscriptionResponse response = subscriptionService.create(request);
        return ApiResponse.success("Subscription created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        SubscriptionResponse response = subscriptionService.findById(id);
        return ApiResponse.success(response);
    }


    @GetMapping
    public ApiResponse<List<SubscriptionResponse>> listSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name="scope") Long scopeId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        List<SubscriptionResponse> response = subscriptionService
                .findAll(scopeId,pageable);

        return ApiResponse.success(response);
    }

//    @GetMapping("/partner/{partnerId}")
//    public ApiResponse<PageResponse<SubscriptionResponse>> listSubscriptionsByPartner(
//            @PathVariable Long partnerId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        PageResponse<SubscriptionResponse> response = subscriptionService
//                .listSubscriptionsByPartner(partnerId, pageable);
//        return ApiResponse.success(response);
//    }


//
//    @GetMapping("/partner/{partnerId}/status/{status}")
//    public ApiResponse<PageResponse<SubscriptionResponse>> listSubscriptionsByPartnerAndStatus(
//            @PathVariable Long partnerId,
//            @PathVariable SubscriptionStatus status,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        PageResponse<SubscriptionResponse> response = subscriptionService
//                .listSubscriptionsByPartnerAndStatus(partnerId, status, pageable);
//        return ApiResponse.success(response);
//    }

    @PutMapping("/{id}")
    public ApiResponse<SubscriptionResponse> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        log.info("Updating subscription: {}", id);
        SubscriptionResponse response = subscriptionService
                .update(id, request);
        return ApiResponse.success("Subscription updated successfully", response);
    }

//    @PostMapping("/{id}/activate")
//    public ApiResponse<SubscriptionResponse> activateSubscription(@PathVariable Long id) {
//        SubscriptionResponse response = subscriptionService.activateSubscription(id);
//        return ApiResponse.success("Subscription activated", response);
//    }
//
//    @PostMapping("/{id}/cancel")
//    public ApiResponse<SubscriptionResponse> cancelSubscription(@PathVariable Long id) {
//        SubscriptionResponse response = subscriptionService.cancelSubscription(id);
//        return ApiResponse.success("Subscription cancelled", response);
//    }
//
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public ApiResponse<Void> deleteSubscription(@PathVariable Long id) {
//        subscriptionService.deleteSubscription(id);
//        return ApiResponse.success("Subscription deleted successfully", null);
//    }
}
