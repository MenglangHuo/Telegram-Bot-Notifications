package com.bronx.notification.mapper;
import com.bronx.notification.dto.company.CompanyRequest;
import com.bronx.notification.dto.company.CompanyResponse;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;
import com.bronx.notification.model.entity.Company;
import com.bronx.notification.model.entity.TelegramTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        uses = {PartnerMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface TelegramTemplateMapper {

    @Mapping(target = "content", source = "htmlContent")
    TelegramTemplate toEntity(TelegramTemplateRequest dto);

    @Mapping(target = "htmlContent", source = "content")
    TelegramTemplateResponse toResponse(TelegramTemplate entity);
}
