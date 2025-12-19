package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.company.CompanyRequest;
import com.bronx.telegram.notification.dto.company.CompanyResponse;
import com.bronx.telegram.notification.dto.employee.EmployeeResponse;
import com.bronx.telegram.notification.dto.organization.SecretRequest;
import com.bronx.telegram.notification.dto.partner.PartnerRequest;
import com.bronx.telegram.notification.dto.partner.PartnerResponse;
import com.bronx.telegram.notification.model.entity.Company;
import com.bronx.telegram.notification.service.CompanyService;
import com.bronx.telegram.notification.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<CompanyResponse> createCompany(@RequestBody CompanyRequest request) {
        return ApiResponse.success("Division created successfully", companyService.createCompany(request));

    }

    @GetMapping("/{id}")
    public ApiResponse<CompanyResponse> getCompany(@PathVariable Long id) {
        return ApiResponse.success(companyService.getCompany(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CompanyResponse> updateCompany(@PathVariable Long id, @RequestBody CompanyRequest request) {
       return ApiResponse.success(companyService.updateCompany(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ApiResponse.success("Successfully deleted company", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<CompanyResponse>> listCompanies(Pageable pageable) {
        return ApiResponse.success(PageResponse.of(companyService.listCompanies(pageable)));
    }

    @GetMapping("/partner/{partnerId}")
    public ApiResponse<PageResponse<CompanyResponse>> listCompaniesByPartner(@PathVariable Long partnerId, Pageable pageable) {
        return ApiResponse.success(PageResponse.of(companyService.listCompaniesByPartner(partnerId, pageable)));
    }
}