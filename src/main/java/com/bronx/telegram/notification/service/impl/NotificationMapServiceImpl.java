package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.checkIn.ChannelRequest;
import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import com.bronx.telegram.notification.exceptions.UnauthorizedException;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.repository.*;
import com.bronx.telegram.notification.service.AuthenticationService;
import com.bronx.telegram.notification.service.NotificationMapService;
import com.bronx.telegram.notification.service.NotificationService;
import com.bronx.telegram.notification.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationMapServiceImpl implements NotificationMapService {
    private final PartnerRepository partnerRepository;
    private final OrganizationRepository organizationRepository;
    private final TelegramChannelRepository channelRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final AuthenticationService authService;

    @Override
    public NotificationChannel mapChannelNotification(String clientId, String secretKey, ChannelRequest request) {
        Partner partner = partnerRepository.findByClientId(clientId)
                .orElseThrow(() -> new UnauthorizedException("Invalid client ID"));

        // 2. Find organization
        Organization org = organizationRepository
                .findByPartnerIdAndOrganizationCode(
                        partner.getId(),
                        request.getOrganizationCode())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        // 3. Find channel by name
        TelegramChannel channel = channelRepository
                .findByOrganizationIdAndChatName(
                        org.getId(),
                        request.getChannelName())
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        // 4. Create notification
        return notificationService.createChannelNotification(
                channel.getSubscription().getId(),
                channel.getId(),
                request
        );
    }

    @Override
    public NotificationPersonal mapPersonalNotification(String clientId, String secretKey, CheckInRequest request) {
        Partner partner = authService.authenticateRequest(clientId, secretKey);

//        Organization org = organizationRepository
//                .findByPartnerIdAndOrganizationCode(
//                        partner.getId(),
//                        request.getOrganizationCode())
//                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Employee employee = employeeRepository
                .findByPartnerIdAndEmployeeCode(
                        partner.getId(),
                        request.getEmployeeCode())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Subscription subscription = subscriptionService
                .getSubscriptionForEmployee(employee);

        return notificationService.createPersonalNotification(
                partner.getId(),
                subscription.getId(),
                employee.getEmployeeCode(),
                request
        );
    }
}
