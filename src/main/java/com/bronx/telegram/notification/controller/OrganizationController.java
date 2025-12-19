package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.organization.OrganizationRequest;
import com.bronx.telegram.notification.dto.organization.OrganizationResponse;
import com.bronx.telegram.notification.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationRequest request
    ) {
        log.info("Creating organization: {}", request.getOrganizationCode());
        OrganizationResponse response = organizationService.createOrganization(request);
        return ApiResponse.success("Organization created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> getOrganization(@PathVariable Long id) {
        OrganizationResponse response = organizationService.getOrganization(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<OrganizationResponse> getOrganizationDetail(
            @PathVariable Long id
    ) {
        OrganizationResponse response = organizationService
                .getOrganization(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<OrganizationResponse>> listOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<OrganizationResponse> response = organizationService
                .listOrganizations(pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/partner/{partnerId}")
    public ApiResponse<PageResponse<OrganizationResponse>> listOrganizationsByPartner(
            @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<OrganizationResponse> response = organizationService
                .listOrganizationsByPartner(partnerId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/partner/{partnerId}/search")
    public ApiResponse<PageResponse<OrganizationResponse>> searchOrganizationsByPartner(
            @PathVariable Long partnerId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<OrganizationResponse> response = organizationService
                .searchOrganizationsByPartner(partnerId, query, pageable);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request
    ) {
        log.info("Updating organization: {}", id);
        OrganizationResponse response = organizationService
                .updateOrganization(id, request);
        return ApiResponse.success("Organization updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ApiResponse.success("Organization deleted successfully", null);
    }
}
