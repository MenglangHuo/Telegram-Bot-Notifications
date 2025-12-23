package com.bronx.telegram.notification.service.impl.webhookCommand.helper;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Company;
import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.RegistrationState;
import com.bronx.telegram.notification.repository.CompanyRepository;
import com.bronx.telegram.notification.repository.EmployeeRepository;
import com.bronx.telegram.notification.service.UserBotStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusHandler {

    private final EmployeeRepository employeeRepository;
    private final MessageSender messageSender;
    private final UserBotStateService userBotStateService;
    private final FormatterUtils formatterUtils;
    private final CompanyRepository companyRepository;

    public void handleStatus(Webhook webhook, WebhookMessage message) {
        String chatId = message.getChatId();
        Long botId = webhook.getBot().getId();

        try {
            log.info("Fetching status for chatId: {}", chatId);

            // Check if user is in registration process
            RegistrationState currentState = userBotStateService.getUserState(chatId);
            if (currentState != RegistrationState.IDLE) {
                sendRegistrationInProgressMessage(botId, chatId, currentState);
                return;
            }

            // Try to find employee by chat ID
            Optional<Employee> employeeOpt = employeeRepository.findByTelegramChatId(chatId);

            if (employeeOpt.isEmpty()) {
                sendNotRegisteredMessage(botId, chatId);
                return;
            }

            sendEmployeeStatus(botId, chatId, employeeOpt.get());

        } catch (Exception e) {
            log.error("Error fetching status for chatId: {}", chatId, e.getMessage());
            messageSender.sendMessage(botId, chatId,
                    "❌ <b>Error</b>\n\n" +
                            "Unable to retrieve your status. Please try again later or contact support.");
        }
    }

    private void sendRegistrationInProgressMessage(Long botId, String chatId,
                                                   RegistrationState currentState) {
        String stateMessage = getRegistrationStateMessage(currentState);
        messageSender.sendMessage(botId, chatId,
                "⏳ <b>Registration In Progress</b>\n\n" +
                        "Current step: " + stateMessage + "\n\n" +
                        "Complete your registration or use /cancel to start over.");
    }

    private void sendNotRegisteredMessage(Long botId, String chatId) {
        messageSender.sendMessage(botId, chatId,
                "❌ <b>Not Registered</b>\n\n" +
                        "You are not registered in the system yet.\n\n" +
                        "📝 Use /register to create your account.");
    }

    private void sendEmployeeStatus(Long botId, String chatId, Employee employee) {
        StringBuilder statusMsg = new StringBuilder();
        statusMsg.append("✅ <b>Registration Status: Active</b>\n\n");

        appendPersonalInfo(statusMsg, employee);
        appendCompanyInfo(statusMsg, employee);
        appendAccountDetails(statusMsg, chatId, employee);

        messageSender.sendMessage(botId, chatId, statusMsg.toString());
        log.info("Status sent successfully for employee: {}", employee.getEmployeeCode());
    }

    private void appendPersonalInfo(StringBuilder sb, Employee employee) {
        sb.append("👤 <b>Personal Information</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━\n");

        if (employee.getFullName() != null && !employee.getFullName().isEmpty()) {
            sb.append("👤 Name: ").append(employee.getFullName()).append("\n");
        }

        if (employee.getEmployeeCode() != null && !employee.getEmployeeCode().isEmpty()) {
            sb.append("🆔 Employee Code: ").append(employee.getEmployeeCode()).append("\n");
        }

        if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
            sb.append("📧 Email: ").append(employee.getEmail()).append("\n");
        }

        if (employee.getContact() != null && !employee.getContact().isEmpty()) {
            sb.append("📱 Contact: ").append(employee.getContact()).append("\n");
        }
        sb.append("\n");
        log.info("successfully appended personal info for employee: {}", employee.getEmployeeCode());
    }

    private void appendCompanyInfo(StringBuilder sb, Employee employee) {
        sb.append("💼 <b>Company Information</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━\n");

        if (employee.getCompany() != null) {
            Company company = companyRepository.findById(employee.getCompany().getId()).get();
            if (company.getName() != null) {
                sb.append("🏢 Company: ").append(formatterUtils.escapeHtml(company.getName())).append("\n");
            }

        }

        if (employee.getRole() != null && !employee.getRole().isEmpty()) {
            sb.append("💼 Position: ").append(formatterUtils.escapeHtml(employee.getRole())).append("\n");
        }
        sb.append("\n");
        log.info("successfully appended company info for employee: {}", employee.getEmployeeCode());
    }

    private void appendAccountDetails(StringBuilder sb, String chatId, Employee employee) {
        sb.append("📊 <b>Account Details</b>\n");
        sb.append("━━━━━━━━━━━━━━━━━\n");

        if (employee.getCreatedAt() != null) {
            sb.append("📅 Registered: ")
                    .append(formatterUtils.formatDate(employee.getCreatedAt())).append("\n");
        }

        if (employee.getUpdatedAt() != null) {
            sb.append("🔄 Last Updated: ")
                    .append(formatterUtils.formatDate(employee.getUpdatedAt())).append("\n");
        }

        sb.append("\n💡 <i>Need to update your info? Contact your administrator.</i>");
    }

    private String getRegistrationStateMessage(RegistrationState state) {
        switch (state) {
            case AWAITING_EMAIL:
                return "Waiting for Email Address";
            case AWAITING_EMP_CODE:
                return "Waiting for Employee Code";
            case AWAITING_CONTACT:
                return "Waiting for Contact Number";
            case AWAITING_FULL_NAME:
                return "Waiting for Full Name";
            case AWAITING_ROLE:
                return "Waiting for Role";
            case AWAITING_FINISH:
                return "Finalizing Registration";
            default:
                return "Unknown";
        }
    }
}
