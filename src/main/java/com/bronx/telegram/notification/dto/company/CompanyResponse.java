package com.bronx.telegram.notification.dto.company;

import com.bronx.telegram.notification.dto.partner.PartnerMainResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompanyResponse(
    Long id,
    String name,
    String code,
    String description,
    PartnerMainResponse partner
    ) {
}
