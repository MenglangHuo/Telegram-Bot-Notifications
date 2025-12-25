package com.bronx.notification.controller;


import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.baseResponse.PageResponse;
import com.bronx.notification.dto.partner.PartnerRequest;
import com.bronx.notification.dto.partner.PartnerResponse;
import com.bronx.notification.dto.partner.SecretRequest;
import com.bronx.notification.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Slf4j
public class PartnerController {
    private final PartnerService partnerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PartnerResponse> createPartner(
            @Valid @RequestBody PartnerRequest request
    ) {
        log.info("Creating partner: {}", request.getPartnerCode());
        PartnerResponse response = partnerService.createPartner(request);
        return ApiResponse.success("Partner created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerResponse> getPartner(@PathVariable Long id) {
        PartnerResponse response = partnerService.getPartner(id);
        return ApiResponse.success(response);
    }


    @PostMapping("/client")
    public ApiResponse<PartnerResponse> getPartnerByClientId(
        @Valid @RequestBody SecretRequest request
    ) {
        PartnerResponse response = partnerService.getPartnerByClientIdAndSecretKey(request.secretKey(), request.credential());
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<PartnerResponse>> listPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<PartnerResponse> response = partnerService.listPartners(pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<PartnerResponse>> searchPartners(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<PartnerResponse> response = partnerService
                .searchPartners(query, pageable);
        return ApiResponse.success(response);
    }

//    @GetMapping("/status/{status}")
//    public ApiResponse<PageResponse<PartnerResponse>> listPartnersByStatus(
//            @PathVariable PartnerStatus status,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//        PageResponse<PartnerResponse> response = partnerService
//                .listPartnersByStatus(status, pageable);
//        return ApiResponse.success(response);
//    }

    @PutMapping("/{id}")
    public ApiResponse<PartnerResponse> updatePartner(
            @PathVariable Long id,
            @Valid @RequestBody PartnerRequest request
    ) {
        log.info("Updating partner: {}", id);
        PartnerResponse response = partnerService.updatePartner(id, request);
        return ApiResponse.success("Partner updated successfully", response);
    }

    @PostMapping("/{id}/regenerate-secret")
    public ApiResponse<PartnerResponse> regenerateSecretKey(@PathVariable Long id) {
        log.info("Regenerating secret key for partner: {}", id);
        PartnerResponse response = partnerService.regenerateSecretKey(id);
        return ApiResponse.success("Secret key regenerated", response);
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<PartnerResponse> activatePartner(@PathVariable Long id) {
        PartnerResponse response = partnerService.activatePartner(id);
        return ApiResponse.success("Partner activated", response);
    }

//    @PostMapping("/{id}/suspend")
//    public ApiResponse<PartnerResponse> suspendPartner(@PathVariable Long id) {
//        PartnerResponse response = partnerService.suspendPartner(id);
//        return ApiResponse.success("Partner suspended", response);
//    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ApiResponse.success("Partner deleted successfully", null);
    }
}
