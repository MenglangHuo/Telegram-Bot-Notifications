package com.bronx.notification.dto.notification;

public interface NotificationDetailProjection {
    Long getNotificationId();
    String getBotName();
    String getOrganizationName();
    String getSubscriptionName();
    Integer getRemainCredit();
    String getCompany();
    String getPartner();
}
