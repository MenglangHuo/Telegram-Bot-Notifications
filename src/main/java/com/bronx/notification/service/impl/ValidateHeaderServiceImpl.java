package com.bronx.notification.service.impl;

import com.bronx.notification.exceptions.UnauthorizedException;
import com.bronx.notification.model.entity.Partner;
import com.bronx.notification.repository.PartnerRepository;
import com.bronx.notification.service.ValidateHeaderService;
import com.bronx.notification.utils.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@RequiredArgsConstructor
public class ValidateHeaderServiceImpl implements ValidateHeaderService {

    private final PartnerRepository partnerRepository;
    @Override
    public void validate(String clientId, String clientSecret) {
        Partner partner = partnerRepository.findByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedException("Invalid client credentials"));
        String decryptedSecret = EncryptionUtils.decrypt(partner.getSecretKey());

        if (!decryptedSecret.equals(clientSecret)) {
            throw new UnauthorizedException("Invalid client credentials");
        }
    }
}
