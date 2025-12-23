package com.bronx.telegram.notification.service.impl;
import com.bronx.telegram.notification.dto.employee.EmployeeTelegramRequest;
import com.bronx.telegram.notification.dto.employee.RegistrationResult;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.exceptions.BusinessException;
import com.bronx.telegram.notification.exceptions.ResourceNotFoundException;
import com.bronx.telegram.notification.model.entity.Company;
import com.bronx.telegram.notification.model.entity.UserBotState;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.model.enumz.RegistrationState;
import com.bronx.telegram.notification.repository.CompanyRepository;
import com.bronx.telegram.notification.repository.EmployeeRepository;
import com.bronx.telegram.notification.repository.UserBotStateRepository;
import com.bronx.telegram.notification.service.UserBotStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBotStateServiceImpl implements UserBotStateService {

    private final UserBotStateRepository stateRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeRegistrationService employeeRegistrationService;

    @Override
    public Company findCompanyByCode(String companyCode) {
        return companyRepository.findByCode(companyCode).orElseThrow(()->new ResourceNotFoundException("Company not found with code: " + companyCode));
    }

    @Override
    public RegistrationState getUserState(String chatId) {
        return stateRepository.findById(chatId)
                .map(UserBotState::getState)
                .orElse(RegistrationState.IDLE);
    }

    @Override
    public void updateUserState(String chatId, RegistrationState newState) {
        UserBotState state = stateRepository.findById(chatId)
                .orElse(new UserBotState());

        state.setChatId(chatId);
        state.setState(newState);
        state.setLastModified(Instant.now());

//        if (newState == RegistrationState.IDLE || newState == RegistrationState.AWAITING_FINISH) {
//            state.setEmail(null);
//            state.setEmployeeCode(null);
//        }

        stateRepository.save(state);
    }

    @Override
    public void saveTemporaryEmail(String chatId, String email) {
        stateRepository.findById(chatId).ifPresent(s -> {
            s.setEmail(email);
            stateRepository.save(s);
        });

    }

    @Override
    public void saveTemporaryEmployeeCode(String chatId, String employeeCode) {
        stateRepository.findById(chatId).ifPresent(s -> {
            s.setEmployeeCode(employeeCode);
            stateRepository.save(s);
        });
    }

    @Override
    public String completeRegistration(Webhook webhook, WebhookMessage message) {
        String chatId = message.getChatId();
        UserBotState state = stateRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException("No registration state found for chatId: " + chatId));

        // Final registration logic
        EmployeeTelegramRequest empTelegramReq=new EmployeeTelegramRequest(
                state.getEmployeeCode(),
                state.getEmail(),
                state.getContact(),
                state.getFullName(),
                webhook.getUserId(),
                webhook.getUsername(),
                chatId,
                state.getRole()
        );
       RegistrationResult result= employeeRegistrationService.registerEmployeeFromTelegram(empTelegramReq);
       if(result.isSuccess()){
           // Reset state to IDLE
           this.updateUserState(chatId, RegistrationState.IDLE);
           return "Success";
       }
         else{
             return "Failed";
         }

    }

    @Override
    public boolean isUserRegistered(String chatId) {
        return employeeRepository.existsByTelegramChatId(chatId);
    }

    @Override
    public void saveFullName(String chatId, String fullName) {
        stateRepository.findById(chatId).ifPresent(s -> {
            s.setFullName(fullName);
            stateRepository.save(s);
        });

    }

    @Override
    public void saveRole(String chatId, String role) {
        stateRepository.findById(chatId).ifPresent(s -> {
            s.setRole(role);
            stateRepository.save(s);
        });
    }

    @Override
    public void saveContact(String chatId, String contact) {
        stateRepository.findById(chatId).ifPresent(s -> {
            s.setContact(contact);
            stateRepository.save(s);
        });
    }
}
