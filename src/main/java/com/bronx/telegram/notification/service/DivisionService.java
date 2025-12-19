package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.division.DivisionRequest;
import com.bronx.telegram.notification.dto.division.DivisionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DivisionService {
    DivisionResponse createDivision(DivisionRequest divisionRequest);
    DivisionResponse updateDivision(Long id,DivisionRequest divisionRequest);
    void deleteDivision(Long id);
    DivisionResponse getDivisionById(Long id);
    PageResponse<DivisionResponse> listDivisions(Pageable pageable);
    PageResponse<DivisionResponse> listDivisionsByOrganization(
            Long organizationId,
            Pageable pageable
    );
    PageResponse<DivisionResponse> searchDivisionsByOrganization(
            Long organizationId,
            String search,
            Pageable pageable
    );
}
