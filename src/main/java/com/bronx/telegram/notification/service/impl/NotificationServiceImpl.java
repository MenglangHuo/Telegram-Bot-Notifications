package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.ValidationResult;
import com.bronx.telegram.notification.dto.checkIn.ChannelRequest;
import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.NotificationEventType;
import com.bronx.telegram.notification.model.enumz.NotificationStatus;
import com.bronx.telegram.notification.repository.*;
import com.bronx.telegram.notification.service.NotificationService;
import com.bronx.telegram.notification.service.SubscriptionValidateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationPersonalRepository personalRepository;
    private final NotificationChannelRepository channelRepository;
    private final EmployeeRepository employeeRepository;
    private final TelegramChannelRepository telegramChannelRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationQueueServiceImpl queueService;
    private final SubscriptionValidateService validationService;

    @Override
    public NotificationPersonal createPersonalNotification(Long partnerId, Long subscriptionId, String employeeCode, CheckInRequest request) {
        // Validate subscription
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        ValidationResult validation = validationService.validateNotificationLimit(subscription);
        if (!validation.isSuccess()) {
            throw new BusinessException(validation.getMessage());
        }

        // Find employee by code
        Employee employee = employeeRepository
                .findByPartnerIdAndEmployeeCode(partnerId, employeeCode)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));


//        if (!employee.isRegisteredWithTelegram()) {
//            throw new IllegalStateException("Employee has not registered with Telegram");
//        }
        // Validate employee belongs to subscription scope
        validateEmployeeInScope(employee, subscription);
        NotificationPersonal notification = new NotificationPersonal();
        notification.setPartner(subscription.getPartner());
        notification.setSubscription(subscription);
        notification.setOrganizationUnit(employee.getOrganizationUnit());
        notification.setEmployee(employee);
        notification.setNotificationType(NotificationEventType.CHECK_IN);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setContent(request.getData());
        notification.setPriority(request.getPriority());
        notification.setStatus(NotificationStatus.QUEUED);
        notification.setQueuedAt(Instant.now());
        notification.setMethod(request.getMethod());
        notification.setLocation(request.getLocation());
        notification.setReceivedAt(Instant.parse(request.getCheckInTime()));
        notification = personalRepository.save(notification);

//        // Increment subscription counter
//        subscription.setNotificationsSentThisMonth(
//                subscription.getNotificationsSentThisMonth() + 1);
        subscriptionRepository.save(subscription);

        // Queue for processing
        queueService.queuePersonalNotification(notification);


        return notification;
    }

    @Override
    public void sendCheckInToHierarchy(Long subscriptionId, String employeeCode, CheckInRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        Employee employee = employeeRepository
                .findByPartnerIdAndEmployeeCode(
                        subscription.getPartner().getId(), employeeCode)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // Find managers who should receive this notification
        List<Employee> recipients = findNotificationRecipients(employee, subscription);

        for (Employee recipient : recipients) {
            if (recipient.isRegisteredWithTelegram()) {
                try {
                    createPersonalNotification(
                            subscription.getPartner().getId(),
                            subscriptionId,
                            recipient.getEmployeeCode(),
                            request
                    );
                } catch (Exception e) {
                    log.error("Failed to send notification to {}: {}",
                            recipient.getEmployeeCode(), e.getMessage());
                }
            }
        }
    }

    @Override
    public NotificationChannel createChannelNotification(Long subscriptionId, Long channelId, ChannelRequest request) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        ValidationResult validation = validationService.validateNotificationLimit(subscription);
        if (!validation.isSuccess()) {
            throw new BusinessException(validation.getMessage());
        }

        TelegramChannel channel = telegramChannelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        // Validate channel belongs to subscription
        if (!channel.getSubscription().getId().equals(subscriptionId)) {
            throw new IllegalArgumentException("Channel does not belong to subscription");
        }


        log.info("notification type: {}",request.getEventType());
        NotificationChannel notification = new NotificationChannel();
        notification.setPartner(subscription.getPartner());
        notification.setSubscription(subscription);
        notification.setOrganizationUnit(channel.getOrganizationUnit());
        notification.setTelegramChannel(channel);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setNotificationType(request.getEventType());
        notification.setPriority(request.getPriority());
        notification.setStatus(NotificationStatus.QUEUED);
        notification.setQueuedAt(Instant.now());
        notification.setReceivedAt(Instant.now());

        notification = channelRepository.save(notification);

        // Increment counter
//        subscription.setNotificationsSentThisMonth(
//                subscription.getNotificationsSentThisMonth() + 1);
        subscriptionRepository.save(subscription);

        queueService.queueChannelNotification(notification);

        log.info("Created channel notification {} for channel {} ",
                notification.getId(), channel.getChatName());

        return notification;
    }

    @Override
    public List<Employee> findNotificationRecipients(Employee employee, Subscription subscription) {
        List<Employee> recipients = new ArrayList<>();

        // Direct manager
        if (employee.getManagerCode() != null) {
            employeeRepository
                    .findByPartnerIdAndEmployeeCode(
                            subscription.getPartner().getId(),
                            employee.getManagerCode())
                    .ifPresent(recipients::add);
        }

        // Find managers in the org unit hierarchy
        OrganizationUnit currentUnit = employee.getOrganizationUnit().getParent();

        while (currentUnit != null && recipients.size() < 3) { // Limit to 3 levels
            // Find employees with management roles in this unit
            List<Employee> managers = employeeRepository
                    .findManagersInOrgUnit(currentUnit.getId());

            managers.stream()
                    .filter(m -> !recipients.contains(m))
                    .forEach(recipients::add);

            currentUnit = currentUnit.getParent();
        }

        return recipients;
    }


    private void validateEmployeeInScope(Employee employee, Subscription subscription) {
        // Check if employee's company matches subscription
        if (!employee.getCompany().getId().equals(subscription.getCompany().getId())) {
            throw new BusinessException("Employee not in subscription company");
        }

        // If subscription has specific scope, check if employee is in that scope
        if (subscription.getScope() != null) {
            List<Long> employeeAncestors = employee.getOrganizationUnit().getAncestorIds();

            if (!employeeAncestors.contains(subscription.getScope().getId()) &&
                    !employee.getOrganizationUnit().getId().equals(subscription.getScope().getId())) {
                throw new BusinessException(
                        "Employee not in subscription scope: " +
                                subscription.getScope().getFullPath());
            }
        }
    }

//    public NotificationPersonal createPersonalNotification(Long companyId, String employeeId,
//                                                           CheckInRequest request) {
//        Company company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
//
//        //get by email
//        Employee employee = employeeRepository.findByCompanyIdAndEmployeeId(companyId, employeeId)
//                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
//
//        if (employee.getTelegramChatId() == null) {
//            throw new IllegalStateException("Employee has not registered with Telegram");
//        }
//
//        NotificationPersonal notification = new NotificationPersonal();
//        notification.setCompany(company);
//        notification.setEmployee(employee);
//        notification.setNotificationType("CHECK_IN");
//        notification.setTitle(request.getTitle());
//        notification.setMessage(request.getMessage());
//        notification.setContent(request.getData());
//        notification.setPriority(request.getPriority());
//        notification.setStatus(NotificationStatus.QUEUED);
//        notification.setQueuedAt(Instant.now());
//        notification.setMethod(request.getMethod());
//        notification.setLocation(request.getLocation());
//        notification.setReceivedAt(LocalDateTime.parse(request.getCheckInTime()));
//        notification = personalRepository.save(notification);
//
//        // Queue for processing
//        queueService.queuePersonalNotification(notification);
//
//        log.info("Created personal notification {} for employee {}",
//                notification.getId(), employeeId);
//
//        return notification;
//    }
//
//
//    public NotificationChannel createChannelNotification(Long companyId,
//                                                         ChannelRequest request) {
//        Company company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
//
//        TelegramChannel channel = telegramChannelRepository.findById(channelId)
//                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
//
//        if (!channel.getCompany().getId().equals(companyId)) {
//            throw new IllegalArgumentException("Channel does not belong to company");
//        }
//
////        if (!channel.getIsBotAdmin()) {
////            throw new IllegalStateException("Bot is not admin in channel");
////        }
//
//        NotificationChannel notification = new NotificationChannel();
//        notification.setCompany(company);
//        notification.setTelegramChannel(channel);
//        notification.setNotificationType(request.getType());
//        notification.setTitle(request.getTitle());
//        notification.setMessage(request.getMessage());
//        notification.setContent(request.getContent());
//        notification.setPriority(request.getPriority());
//        notification.setStatus(NotificationStatus.QUEUED);
//        notification.setQueuedAt(Instant.now());
//        notification = channelRepository.save(notification);
//
//        // Queue for processing
//        queueService.queueChannelNotification(notification);
//
//        log.info("Created channel notification {} for channel {}",
//                notification.getId(), channel.getChatName());
//
//        return notification;
//    }

//    public void sendCheckInNotification(Long companyId, CheckInRequest request) {
//        // Find manager or head manager employees
//        Company com=companyRepository.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Company not found"));
//        List<Employee> managers = employeeRepository.findManagersByCompany(com);
//
//        for (Employee manager : managers) {
//            if (manager.getTelegramChatId() != null) {
//                NotificationRequest notifRequest = NotificationRequest.builder()
//                        .type("CHECK_IN")
//                        .title("Check-In Alert")
//                        .message(formatCheckInMessage(request))
//                        .priority(NotificationPriority.HIGH)
//                        .build();
//
//                createPersonalNotification(companyId, manager.getEmployeeId(), request);
//            }
//        }
//    }

//    private String formatCheckInMessage(CheckInRequest request) {
//        return String.format(
//                "<b>Check-In Alert</b>\n\n" +
//                        "Employee: %s\n" +
//                        "Time: %s\n" +
//                        "Location: %s\n" +
//                        "Status: %s",
//                        "Method: %s",
//                request.getEmployeeName(),
//                request.getCheckInTime(),
//                request.getLocation(),
//                request.getStatus(),
//                request.getMethod()
//        );
//    }
}
