package com.bronx.telegram.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class CompanyRequest{
   private String companyName;
   private String clientId;
   private String clientPassword;
   private String webhookSecret;

}
