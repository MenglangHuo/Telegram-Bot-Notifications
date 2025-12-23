package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.employee.*;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.EmployeeMapper;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.model.enumz.SubscriptionStatus;
import com.bronx.telegram.notification.repository.*;
import com.bronx.telegram.notification.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor

public class EmployeeRegistrationService {
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final OrganizationUnitRepository organizationUnitRepository;
    private final TelegramBotService telegramBotService;
    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeMapper employeeMapper;

    public RegistrationResult registerEmployeeFromTelegram(
            EmployeeTelegramRequest request
    ){
        log.info("🔍 Registration attempt - Email: {}",
                request.email());
        Employee employee = employeeRepository.findByEmployeeCode(request.employeeCode()).orElse(null);
        if(employee==null && request.email()!=null) {
            employee = employeeRepository.findByEmail(request.email()).orElse(null);
        }
        if (employee == null) {
            log.warn("❌ Employee not found ");
            return RegistrationResult.notFound(
                    "Employee not found. Please contact your HR department.");
        }
        employee.setContact(request.contact());
        employee.setTelegramUserId(request.telegramUserId());
        employee.setTelegramUsername(request.telegramUsername());
        employee.setTelegramChatId(request.telegramChatId());
        employee.setRegisteredAt(Instant.now());
        employee.setStatus(BotStatus.ACTIVE);

        if(request.employeeCode()!=null)
            employee.setEmployeeCode(request.employeeCode());
        if(request.email()!=null)
            employee.setEmail(request.email());
        if(request.fullName()!=null)
            employee.setFullName(request.fullName());
        if(request.role()!=null)
            employee.setRole(request.role());

        try{
            return RegistrationResult.success(employeeRepository.save(employee));
        }catch (Exception e){
            log.error("❌ Error during employee registration: {}", e.getMessage(), e);
            return RegistrationResult.failure(
                    "An error occurred during registration. Please try again later.");
        }

    }
    public RegistrationResult registerEmployee(
            String email,
            String telegramUserId,
            String telegramUsername,
            String chatId) {

        log.info("🔍 Registration attempt - Email: {}, TelegramID: {}, ChatID: {}",
                email, telegramUserId, chatId);

        try {
            // Find employee by email (search across all partners)
            Employee employee = employeeRepository.findByEmail(email)
                    .orElse(null);

            if (employee == null) {
                log.warn("❌ Employee not found with email: {}", email);
                return RegistrationResult.notFound(
                        "Employee not found. Please contact your HR department.");
            }

            // Check if employee is already registered with a DIFFERENT Telegram account
            if (employee.getTelegramUserId() != null && !employee.getTelegramUserId().isEmpty()&&
                    !employee.getTelegramUserId().equals(telegramUserId)) {
                log.warn("⚠️ Employee {} already registered with different Telegram account",
                        email);
                return RegistrationResult.alreadyRegistered(
                        "This employee account is already linked to another Telegram account. " +
                                "Please contact your HR department to reset.");
            }

            // Check if employee is already fully registered
            if (employee.getTelegramUserId() != null &&
                    employee.getTelegramChatId() != null){
                log.info("ℹ️ Employee {} already registered", email);
                return RegistrationResult.alreadyRegistered(
                        "You are already registered. Welcome back!");
            }

            // Check if employee is terminated
            if (employee.getIsActive().equals(Boolean.FALSE)) {
                log.warn("❌ Terminated employee {} attempted registration", email);
                return RegistrationResult.failure(
                        "Your employee account is no longer active. " +
                                "Please contact HR for assistance.");
            }

            // Update employee with Telegram information
            employee.setTelegramUserId(telegramUserId);
            employee.setTelegramUsername(telegramUsername);
            employee.setTelegramChatId(chatId);
            employee.setRegisteredAt(Instant.now());

            Employee empUpdated = employeeRepository.save(employee);

            log.info("✅ Successfully registered employee {} ({}) with Telegram",
                    empUpdated.getFullName(),
                    empUpdated.getEmployeeCode());

            // Send welcome message
//            sendWelcomeMessage(empUpdated);

            return RegistrationResult.success(empUpdated);

        } catch (Exception e) {
            log.error("❌ Error during employee registration: {}", e.getMessage(), e);
            return RegistrationResult.failure(
                    "An error occurred during registration. Please try again later.");
        }
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("📝 Creating employee: {} ({})", request.fullName(), request.email());

        // Fetch company
        Company company = companyRepository
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Fetch organization unit
        OrganizationUnit orgUnit = organizationUnitRepository
                .findById(request.organizationUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization unit not found"));

        // Validate org unit belongs to company
        if (!orgUnit.getCompany().getId().equals(company.getId())) {
            throw new BusinessException("Organization unit does not belong to the specified company");
        }

        Partner partner = company.getPartner();

        // Check for duplicates
        if (employeeRepository.existsByPartnerIdAndEmail(partner.getId(), request.email())) {
            throw new BusinessException("Employee with this email already exists");
        }

        if (employeeRepository.existsByPartnerIdAndEmployeeCode(
                partner.getId(), request.employeeCode())) {
            throw new BusinessException("Employee code already exists");
        }

        // Create employee
        Employee employee = Employee.builder()
                .partner(partner)
                .company(company)
                .organizationUnit(orgUnit)
                .employeeCode(request.employeeCode())
                .managerCode(request.managerCode())
                .email(request.email())
                .fullName(request.fullName())
                .role(request.role())
                .contact(request.contact())
                .telegramUsername(request.telegramUsername())
                .status(BotStatus.ACTIVE)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("✅ Created employee: {} in org unit: {}",
                saved.getEmployeeCode(), orgUnit.getUnitName());

        return employeeMapper.toResponse(saved);
    }

    private void sendWelcomeMessage(Employee employee) {
        try {
            // Get bot for employee's organization
            Subscription subscription = employee.getOrganizationUnit() != null ?
                    getSubscriptionForEmployee(employee) : null;

            if (subscription == null) {
                log.warn("No subscription found for employee {}", employee.getEmployeeCode());
                return;
            }

            TelegramBot bot = telegramBotService.getBotForSubscription(subscription.getId());
            if (bot == null) {
                log.warn("No bot found for subscription {}", subscription.getId());
                return;
            }

            String welcomeMessage = String.format("""
                <b>Welcome to %s! 🎉</b>
                
                Hi %s,
                
                Your Telegram account has been successfully registered!
                
                You will now receive notifications about:
                • Company announcements
                • Team updates
                • Check-in confirmations
                • Important alerts
                
                Use /help to see available commands.
                
                Have a great day! 🚀
                """,
                    employee.getOrganizationUnit().getUnitName(),
                    employee.getFullName()
            );

            telegramBotService.sendPersonalMessage(
                    bot.getId(),
                    employee.getTelegramChatId(),
                    welcomeMessage,
                    false
            );

            log.info("📤 Sent welcome message to employee: {}", employee.getEmployeeCode());

        } catch (Exception e) {
            log.error("❌ Failed to send welcome message: {}", e.getMessage(), e);
        }
    }

    // Other private methods updated similarly (e.g., isValidEmail remains)
    // sendWelcomeMessage: Use company.getName() instead of organization.getOrganizationName()
    // getSubscriptionForEmployee: Refactor to use hierarchy (traverse ancestors for subscriptions)
    private Subscription getSubscriptionForEmployee(Employee employee) {

        OrganizationUnit unit = employee.getOrganizationUnit();

        while (unit != null) {
            Optional<Subscription> sub = subscriptionRepository.findByScopeId(unit.getId());
            if (sub.isPresent() && sub.get().getStatus() == SubscriptionStatus.ACTIVE) {
                return sub.get();
            }
            unit = unit.getParent();
        }
        return subscriptionRepository.findByCompanyIdAndScopeIsNull(employee.getCompany().getId())
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

}
