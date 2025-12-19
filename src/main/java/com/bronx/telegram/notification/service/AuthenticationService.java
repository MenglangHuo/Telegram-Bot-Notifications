package com.bronx.telegram.notification.service;
import com.bronx.telegram.notification.exceptions.UnauthorizedException;
import com.bronx.telegram.notification.model.entity.Partner;
import com.bronx.telegram.notification.repository.PartnerRepository;
import com.bronx.telegram.notification.service.impl.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final PartnerRepository partnerRepository;
    private final EncryptionService encryptionService;


    public Partner authenticateRequest(String clientId, String secretKey) {
        Partner partner = partnerRepository.findByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedException("Invalid client credentials"));

        String decryptedSecret = encryptionService.decrypt(partner.getSecretKey());

//        if (!decryptedSecret.equals(secretKey)) {
//            throw new UnauthorizedException("Invalid client credentials");
//        }

        return partner;
    }

//    private void logFailedRequest(Company company, HttpServletRequest request, String reason) {
//        ApiRequestLog log = new ApiRequestLog();
//        log.setCompany(company);
//        log.setClientId(company.getClientId());
//        log.setEndpoint(request.getRequestURI());
//        log.setHttpMethod(request.getMethod());
//        log.setIpAddress(getClientIp(request));
//        log.setSuccess(false);
//        log.setErrorMessage(reason);
//
//
////        apiLogRepository.save(log);
//    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
