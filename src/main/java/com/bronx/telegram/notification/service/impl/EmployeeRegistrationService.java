package com.bronx.telegram.notification.service.impl;

import com.bronx.telegram.notification.dto.ValidationResult;
import com.bronx.telegram.notification.dto.employee.*;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.mapper.EmployeeMapper;
import com.bronx.telegram.notification.model.entity.*;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.repository.*;
import com.bronx.telegram.notification.service.TelegramBotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor

public class EmployeeRegistrationService {
    private final EmployeeRepository employeeRepository;
    private final PartnerRepository partnerRepository;
    private final OrganizationRepository organizationRepository;
    private final DivisionRepository divisionRepository;
    private final DepartmentRepository departmentRepository;
    private final TelegramBotService telegramBotService;
    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeMapper employeeMapper;

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
            if (employee.getTelegramUserId() != null &&
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
            sendWelcomeMessage(empUpdated);

            return RegistrationResult.success(empUpdated);

        } catch (Exception e) {
            log.error("❌ Error during employee registration: {}", e.getMessage(), e);
            return RegistrationResult.failure(
                    "An error occurred during registration. Please try again later.");
        }
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        log.info("📝 Creating employee: {} ({})", request.fullName(), request.email());

        // Validate request
        ValidationResult validation = validateEmployeeRequest(request);
        if (!validation.isSuccess()) {
            throw new BusinessException(validation.getMessage());
        }

        Organization organization = organizationRepository
                .findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Check for duplicates
        if (employeeRepository.existsByPartnerIdAndEmail(
                organization.getPartner().getId(), request.email())) {
            throw new BusinessException("Employee with this email already exists");
        }

        if (employeeRepository.existsByPartnerIdAndEmployeeCode(
                organization.getPartner().getId(), request.employeeCode())) {
            throw new BusinessException("Employee code already exists");
        }

        // Fetch and validate entities
        Partner partner = partnerRepository.findById(organization.getPartner().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Division division = null;
        if (request.divisionId() != null) {
            division = divisionRepository.findById(request.divisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Division not found"));
            validateDivision(division, organization);
        }

        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            validateDepartment(department, organization, division);
        }

        // Create employee
        Employee employee = Employee.builder()
                .partner(partner)
                .organization(organization)
                .division(division)
                .department(department)
                .employeeCode(request.employeeCode())
                .managerCode(request.managerCode())
                .email(request.email())
                .fullName(request.fullName())
                .role(request.role())
                .contact(request.contact())
                .telegramUsername(request.telegramUsername())
                .status(BotStatus.PENDING)
                .isManager(request.isManager() != null ? request.isManager() : false)
                .isHeadOfDepartment(request.isHeadOfDepartment() != null ?
                        request.isHeadOfDepartment() : false)
                .isHeadOfDivision(request.isHeadOfDivision() != null ?
                        request.isHeadOfDivision() : false)
                .build();
        return employeeMapper.toResponse(employeeRepository.save(employee));

    }
    public BatchEmployeeResult batchCreateEmployee(List<EmployeeRequest> employeeRequests) {
        log.info("📥 Starting batch employee creation for {} employees",
                employeeRequests.size());

        List<Employee> successfulEmployees = new ArrayList<>();
        List<EmployeeCreationError> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < employeeRequests.size(); i++) {
            EmployeeRequest empReq = employeeRequests.get(i);

            try {
                // Validate request
                ValidationResult validation = validateEmployeeRequest(empReq);
                if (!validation.isSuccess()) {
                    errors.add(new EmployeeCreationError(
                            i, empReq.email(), validation.getMessage()));
                    errorCount++;
                    continue;
                }



                Organization organization = organizationRepository
                        .findById(empReq.organizationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

                // Validate organization belongs to partner
                // Check for duplicates
                if (employeeRepository.existsByPartnerIdAndEmail(
                        organization.getPartner().getId(), empReq.email())) {
                    errors.add(new EmployeeCreationError(
                            i, empReq.email(), "Employee with this email already exists"));
                    errorCount++;
                    continue;
                }

                if (employeeRepository.existsByPartnerIdAndEmployeeCode(
                        organization.getPartner().getId(), empReq.employeeCode())) {
                    errors.add(new EmployeeCreationError(
                            i, empReq.email(), "Employee code already exists"));
                    errorCount++;
                    continue;
                }

                // Fetch related entities
                Partner partner = partnerRepository.findById(organization.getPartner().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
                Division division = null;
                if (empReq.divisionId() != null) {
                    division = divisionRepository.findById(empReq.divisionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Division not found"));

                    if (!division.getOrganization().getId().equals(organization.getId())) {
                        throw new BusinessException("Division does not belong to organization");
                    }
                }

                Department department = null;
                if (empReq.departmentId() != null) {
                    department = departmentRepository.findById(empReq.departmentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

                    if (!department.getOrganization().getId().equals(organization.getId())) {
                        throw new BusinessException(
                                "Department does not belong to organization");
                    }

                    // If department has division, it must match
                    if (department.getDivision() != null && division != null &&
                            !department.getDivision().getId().equals(division.getId())) {
                        throw new BusinessException(
                                "Department division mismatch");
                    }
                }

                // Create employee
                Employee employee = Employee.builder()
                        .partner(partner)
                        .organization(organization)
                        .division(division)
                        .department(department)
                        .employeeCode(empReq.employeeCode())
                        .managerCode(empReq.managerCode())
                        .email(empReq.email())
                        .fullName(empReq.fullName())
                        .role(empReq.role())
                        .contact(empReq.contact())
                        .telegramUsername(empReq.telegramUsername())
                        .status(BotStatus.PENDING) // PENDING until Telegram registration
                        .isManager(empReq.isManager() != null ? empReq.isManager() : false)
                        .isHeadOfDepartment(empReq.isHeadOfDepartment() != null ?
                                empReq.isHeadOfDepartment() : false)
                        .isHeadOfDivision(empReq.isHeadOfDivision() != null ?
                                empReq.isHeadOfDivision() : false)
                        .build();

                Employee saved = employeeRepository.save(employee);
                successfulEmployees.add(saved);
                successCount++;

                log.debug("✅ Created employee: {} ({})",
                        saved.getFullName(), saved.getEmployeeCode());

            } catch (ResourceNotFoundException e) {
                errors.add(new EmployeeCreationError(
                        i, empReq.email(), e.getMessage()));
                errorCount++;
                log.error("❌ Entity not found for employee {}: {}",
                        empReq.email(), e.getMessage());
            } catch (BusinessException e) {
                errors.add(new EmployeeCreationError(
                        i, empReq.email(), e.getMessage()));
                errorCount++;
                log.error("❌ Business validation failed for employee {}: {}",
                        empReq.email(), e.getMessage());
            } catch (Exception e) {
                errors.add(new EmployeeCreationError(
                        i, empReq.email(), "Unexpected error: " + e.getMessage()));
                errorCount++;
                log.error("❌ Unexpected error creating employee {}: {}",
                        empReq.email(), e.getMessage(), e);
            }
        }

        log.info("📊 Batch creation completed - Success: {}, Errors: {}",
                successCount, errorCount);

        return new BatchEmployeeResult(successfulEmployees, errors, successCount, errorCount);
    }
    @Transactional
    public Employee updateEmployee(Long employeeId, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        log.info("📝 Updating employee: {}", employee.getEmployeeCode());

        // Update basic info
        if (request.fullName() != null) {
            employee.setFullName(request.fullName());
        }
        if (request.email() != null && !request.email().equals(employee.getEmail())) {
            // Check if new email is available
            if (employeeRepository.existsByPartnerIdAndEmail(
                    employee.getPartner().getId(), request.email())) {
                throw new BusinessException("Email already in use");
            }
            employee.setEmail(request.email());
        }
        if (request.contact() != null) {
            employee.setContact(request.contact());
        }
        if (request.role() != null) {
            employee.setRole(request.role());
        }
        if (request.managerCode() != null) {
            employee.setManagerCode(request.managerCode());
        }

        // Update hierarchy flags
        if (request.isManager() != null) {
            employee.setIsManager(request.isManager());
        }
        if (request.isHeadOfDepartment() != null) {
            employee.setIsHeadOfDepartment(request.isHeadOfDepartment());
        }
        if (request.isHeadOfDivision() != null) {
            employee.setIsHeadOfDivision(request.isHeadOfDivision());
        }

        // Update organizational structure
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            validateDepartment(department, employee.getOrganization(), employee.getDivision());
            employee.setDepartment(department);
        }

        if (request.divisionId() != null) {
            Division division = divisionRepository.findById(request.divisionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Division not found"));
            validateDivision(division, employee.getOrganization());
            employee.setDivision(division);
        }

        Employee updated = employeeRepository.save(employee);
        log.info("✅ Updated employee: {}", updated.getEmployeeCode());

        return updated;
    }
    @Transactional
    public void terminateEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        log.info("🚫 Terminating employee: {} ({})",
                employee.getFullName(), employee.getEmployeeCode());

        employee.setStatus(BotStatus.TERMINATE);
        employee.setDeletedAt(Instant.now());

        // Clear sensitive information
        employee.setTelegramUserId(null);
        employee.setTelegramChatId(null);
        employee.setTelegramUsername(null);

        employeeRepository.save(employee);

        log.info("✅ Employee terminated: {}", employee.getEmployeeCode());
    }
    @Transactional
    public void reactivateEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getStatus() != BotStatus.TERMINATE) {
            throw new BusinessException("Employee is not terminated");
        }

        log.info("🔄 Reactivating employee: {} ({})",
                employee.getFullName(), employee.getEmployeeCode());

        employee.setStatus(BotStatus.PENDING);
        employee.setDeletedAt(null);

        employeeRepository.save(employee);

        log.info("✅ Employee reactivated: {}", employee.getEmployeeCode());
    }
    @Transactional
    public void unregisterFromTelegram(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        log.info("📱 Unregistering employee from Telegram: {}", employee.getEmployeeCode());

        employee.setTelegramUserId(null);
        employee.setTelegramChatId(null);
        employee.setTelegramUsername(null);
        employee.setStatus(BotStatus.PENDING);
        employee.setRegisteredAt(null);

        employeeRepository.save(employee);

        log.info("✅ Employee unregistered from Telegram: {}", employee.getEmployeeCode());
    }
    private ValidationResult validateEmployeeRequest(EmployeeRequest request) {

        if (request.organizationId() == null) {
            return ValidationResult.failure("Organization ID is required");
        }
        if (request.employeeCode() == null || request.employeeCode().trim().isEmpty()) {
            return ValidationResult.failure("Employee code is required");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            return ValidationResult.failure("Email is required");
        }
        if (!isValidEmail(request.email())) {
            return ValidationResult.failure("Invalid email format");
        }
        if (request.fullName() == null || request.fullName().trim().isEmpty()) {
            return ValidationResult.failure("Full name is required");
        }
        return ValidationResult.success();
    }

    private void validateDivision(Division division, Organization organization) {
        if (!division.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("Division does not belong to organization");
        }
    }

    private void validateDepartment(
            Department department,
            Organization organization,
            Division division) {

        if (!department.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("Department does not belong to organization");
        }

        if (department.getDivision() != null && division != null &&
                !department.getDivision().getId().equals(division.getId())) {
            throw new BusinessException("Department does not belong to specified division");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    private void sendWelcomeMessage(Employee employee) {
        try {
            // Get bot for employee's organization
            Subscription subscription = employee.getOrganization() != null ?
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
                    employee.getOrganization().getOrganizationName(),
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

    private Subscription getSubscriptionForEmployee(Employee employee) {
        // Try department subscription first
        if (employee.getDepartment() != null) {
            Optional<Subscription> deptSub = subscriptionRepository
                    .findActiveDepartmentSubscription(employee.getDepartment().getId());
            if (deptSub.isPresent()) {
                return deptSub.get();
            }
        }

        // Try division subscription
        if (employee.getDivision() != null) {
            Optional<Subscription> divSub = subscriptionRepository
                    .findActiveDivisionSubscription(employee.getDivision().getId());
            if (divSub.isPresent()) {
                return divSub.get();
            }
        }

        // Fall back to organization subscription
        if (employee.getOrganization() != null) {
            return subscriptionRepository
                    .findActiveOrganizationSubscription(employee.getOrganization().getId())
                    .orElse(null);
        }

        return null;
    }

}
