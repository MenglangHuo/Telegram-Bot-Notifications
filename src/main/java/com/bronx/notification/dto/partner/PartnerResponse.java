package com.bronx.notification.dto.partner;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerResponse {
    private Long id;
    private String partnerName;
    private String partnerCode;
    private String clientId;
    private String secretKey;
    private String contactName;
    private String contactEmail;
    private String contact;
    //audit
    private Instant createdAt;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
}
