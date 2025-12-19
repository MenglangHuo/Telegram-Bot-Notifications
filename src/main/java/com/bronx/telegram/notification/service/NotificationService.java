package com.bronx.telegram.notification.service;
import com.bronx.telegram.notification.dto.checkIn.ChannelRequest;
import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
import com.bronx.telegram.notification.model.entity.Subscription;

import java.util.List;

public interface NotificationService {
//    NotificationPersonal createPersonalNotification(Long companyId, String employeeId,
//                                                    CheckInRequest request);
//
//    NotificationChannel createChannelNotification(Long companyId,
//                                                  ChannelRequest request);

    NotificationPersonal createPersonalNotification(
            Long partnerId,
            Long subscriptionId,
            String employeeCode,
            CheckInRequest request);
    void sendCheckInToHierarchy(
            Long subscriptionId,
            String employeeCode,
            CheckInRequest request);

    NotificationChannel createChannelNotification(
            Long subscriptionId,
            Long channelId,
            ChannelRequest request);

    List<Employee> findNotificationRecipients(
            Employee employee,
            Subscription subscription);
//
//    NotificationPersonal checkPersonalNotification(String clientId,String secretKey,CheckInRequest request);
//    NotificationChannel checkChannelNotification(String clientId,String secretKey,ChannelRequest request);

}
