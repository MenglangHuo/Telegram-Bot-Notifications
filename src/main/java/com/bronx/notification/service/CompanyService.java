package com.bronx.notification.service;

import com.bronx.notification.dto.company.CompanyRequest;
import com.bronx.notification.dto.company.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {
    CompanyResponse createCompany(CompanyRequest request);
    CompanyResponse getCompany(Long id);
    CompanyResponse updateCompany(Long id, CompanyRequest request);
    void deleteCompany(Long id);
    Page<CompanyResponse> listCompanies(Pageable pageable);
    Page<CompanyResponse> listCompaniesByPartner(Long partnerId, Pageable pageable);
}
