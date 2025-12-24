package com.bronx.telegram.notification.dto.partner;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartnerMainResponse(
        Long id,
        String partnerName,
        String partnerCode
) {
}
