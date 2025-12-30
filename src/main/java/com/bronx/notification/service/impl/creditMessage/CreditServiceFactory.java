package com.bronx.notification.service.impl.creditMessage;

import com.bronx.notification.configs.CreditConfig;
import com.bronx.notification.model.enumz.CreditMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
/**
 * Factory for obtaining the appropriate CreditService based on configuration.
 * Allows runtime switching between Redis batch mode and direct DB mode.
 */
@Component
@Slf4j
public class CreditServiceFactory {
    private final CreditService redisCreditService;
    private final CreditService directDbCreditService;
    private final CreditConfig creditConfig;
    public CreditServiceFactory(
            @Qualifier("redisCreditService") CreditService redisCreditService,
            @Qualifier("directDbCreditService") CreditService directDbCreditService,
            CreditConfig creditConfig) {
        this.redisCreditService = redisCreditService;
        this.directDbCreditService = directDbCreditService;
        this.creditConfig = creditConfig;
        log.info("CreditServiceFactory initialized with mode: {}", creditConfig.getMode());
    }
    /**
     * Get the current credit service based on configuration
     */
    public CreditService getCreditService() {
        if (creditConfig.isRedisBatchMode()) {
            log.info("Redis Mode for Credit Notification");
            return redisCreditService;
        }
        return directDbCreditService;
    }
    /**
     * Get a specific credit service by mode
     */
    public CreditService getCreditService(CreditMode mode) {
        return switch (mode) {
            case REDIS_BATCH -> redisCreditService;
            case DIRECT_DB -> directDbCreditService;
        };
    }
    /**
     * Check if currently using Redis batch mode
     */
    public boolean isRedisBatchMode() {
        return creditConfig.isRedisBatchMode();
    }
    /**
     * Get current mode
     */
    public CreditMode getCurrentMode() {
        return creditConfig.getMode();
    }
}

