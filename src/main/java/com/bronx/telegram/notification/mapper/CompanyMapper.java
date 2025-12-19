package com.bronx.telegram.notification.mapper;


import com.bronx.telegram.notification.dto.company.CompanyRequest;
import com.bronx.telegram.notification.dto.company.CompanyResponse;
import com.bronx.telegram.notification.model.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = {PartnerMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface CompanyMapper {

    Company toEntity(CompanyRequest companyRequest);
    CompanyResponse toResponse(Company company);
    void updateCompany(CompanyRequest companyRequest, @MappingTarget Company company);
}
