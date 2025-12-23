package com.bronx.telegram.notification.service;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Company;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.RegistrationState;

public interface UserBotStateService {
    Company findCompanyByCode(String companyCode);
    RegistrationState getUserState(String chatId);
    void updateUserState(String chatId, RegistrationState newState);
    void saveTemporaryEmail(String chatId, String email);
    void saveTemporaryEmployeeCode(String chatId, String employeeCode);
    String completeRegistration(Webhook webhook, WebhookMessage message);
    boolean isUserRegistered(String chatId);
    void saveFullName(String chatId, String fullName);
    void saveRole(String chatId, String role);
    void saveContact(String chatId, String contact);

}
