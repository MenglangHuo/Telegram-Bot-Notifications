package com.bronx.notification.dto.partner;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRequest {
    @NotBlank(message = "Partner name is required")
    @Size(max = 100, message = "Partner name must not exceed 100 characters")
    private String partnerName;

    @NotBlank(message = "ClientId is required")
    @Size(max = 100, message = "ClientId name must not exceed 100 characters")
    private String clientId;


    @NotBlank(message = "Partner code is required")
    @Size(max = 50, message = "Partner code must not exceed 50 characters")
    private String partnerCode;


    @NotBlank(message = "Contact name is required")
    @Size(max = 70)
    private String contactName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 70)
    private String contactEmail;

    @Size(max = 30)
    private String contact;

}
