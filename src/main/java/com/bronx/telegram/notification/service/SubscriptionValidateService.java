package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.ValidationResult;
import com.bronx.telegram.notification.model.entity.Subscription;

public interface SubscriptionValidateService {
    boolean canCreateBot(Subscription subscription);
    boolean canAddChannel(Subscription subscription);
    ValidationResult validateNotificationLimit(Subscription subscription);
    void resetMonthlyCounters();
}
