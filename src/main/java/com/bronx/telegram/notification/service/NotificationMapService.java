package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.checkIn.ChannelRequest;
import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;

public interface NotificationMapService {
    NotificationChannel mapChannelNotification(String clientId, String secretKey, ChannelRequest request);
    NotificationPersonal mapPersonalNotification(String clientId, String secretKey, CheckInRequest request);
}
