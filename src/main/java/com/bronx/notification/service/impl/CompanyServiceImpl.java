package com.bronx.notification.service.impl;

import com.bronx.notification.dto.company.CompanyRequest;
import com.bronx.notification.dto.company.CompanyResponse;
import com.bronx.notification.exceptions.BusinessException;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.CompanyMapper;
import com.bronx.notification.model.entity.Company;
import com.bronx.notification.model.entity.Partner;
import com.bronx.notification.repository.CompanyRepository;
import com.bronx.notification.repository.PartnerRepository;
import com.bronx.notification.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final PartnerRepository partnerRepository;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Partner partner = partnerRepository.findById(request.partnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        if (companyRepository.existsByPartnerIdAndCode(partner.getId(), request.code())) {
            throw new BusinessException("Company code already exists for this partner");
        }
        Company company = companyMapper.toEntity(request);
        company.setPartner(partner);
        Company saved = companyRepository.save(company);
        log.info("Created company {} for partner {}", saved.getId(), partner.getId());
        return companyMapper.toResponse(saved);
    }

    @Override
    public CompanyResponse getCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        if (request.code() != null && !request.code().equals(company.getCode())) {
            if (companyRepository.existsByPartnerIdAndCode(company.getPartner().getId(), request.code())) {
                throw new BusinessException("Company code already exists for this partner");
            }
            company.setCode(request.code());
        }
        companyMapper.updateCompany(request, company);
        Company updated = companyRepository.save(company);
        log.info("Updated company {}", id);
        return companyMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        // Check for dependencies (e.g., organization units, employees) before delete
        // For example: if (organizationUnitRepository.existsByCompanyId(id)) { throw new BusinessException("Cannot delete company with organization units"); }
        company.setDeletedAt(Instant.now()); // Soft delete
        companyRepository.save(company);
        log.info("Deleted company {}", id);
    }

    @Override
    public Page<CompanyResponse> listCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(companyMapper::toResponse);
    }

    @Override
    public Page<CompanyResponse> listCompaniesByPartner(Long partnerId, Pageable pageable) {
        // Assuming a custom query in repository: Page<Company> findByPartnerId(Long partnerId, Pageable pageable);
        return companyRepository.findAllByPartnerIdAndDeletedAtIsNull(partnerId,pageable).map(companyMapper::toResponse);
    }
}
