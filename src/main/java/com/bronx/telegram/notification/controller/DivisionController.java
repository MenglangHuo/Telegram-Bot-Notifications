package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.division.DivisionRequest;
import com.bronx.telegram.notification.dto.division.DivisionResponse;
import com.bronx.telegram.notification.service.DivisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/divisions")
@Slf4j
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionService divisionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DivisionResponse> createDivision(
            @Valid @RequestBody DivisionRequest request
    ) {
        log.info("Creating division: {}", request.getDivisionCode());
        DivisionResponse response = divisionService.createDivision(request);
        return ApiResponse.success("Division created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<DivisionResponse> getDivision(@PathVariable Long id) {
        DivisionResponse response = divisionService.getDivisionById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<DivisionResponse> getDivisionDetail(@PathVariable Long id) {
        DivisionResponse response = divisionService.getDivisionById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<DivisionResponse>> listDivisions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponse<DivisionResponse> response = divisionService.listDivisions(pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/organization/{organizationId}")
    public ApiResponse<PageResponse<DivisionResponse>> listDivisionsByOrganization(
            @PathVariable Long organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DivisionResponse> response = divisionService
                .listDivisionsByOrganization(organizationId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/organization/{organizationId}/search")
    public ApiResponse<PageResponse<DivisionResponse>> searchDivisionsByOrganization(
            @PathVariable Long organizationId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DivisionResponse> response = divisionService
                .searchDivisionsByOrganization(organizationId, query, pageable);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<DivisionResponse> updateDivision(
            @PathVariable Long id,
            @Valid @RequestBody DivisionRequest request
    ) {
        log.info("Updating division: {}", id);
        DivisionResponse response = divisionService.updateDivision(id, request);
        return ApiResponse.success("Division updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteDivision(@PathVariable Long id) {
        divisionService.deleteDivision(id);
        return ApiResponse.success("Division deleted successfully", null);
    }
}
