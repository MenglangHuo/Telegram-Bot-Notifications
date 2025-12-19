package com.bronx.telegram.notification.dto.employee;

import com.bronx.telegram.notification.model.entity.Employee;
import com.bronx.telegram.notification.model.enumz.RegistrationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResult {
    private boolean success;
    private String message;
    private Employee employee;
    private RegistrationStatus registrationStatus;


    public static RegistrationResult success(Employee employee) {
        return RegistrationResult.builder()
                .success(true)
                .message("Registration successful")
                .employee(employee)
                .registrationStatus(RegistrationStatus.SUCCESS)
                .build();
    }

    public static RegistrationResult notFound(String message) {
        return RegistrationResult.builder()
                .success(false)
                .message(message)
                .registrationStatus(RegistrationStatus.NOT_FOUND)
                .build();
    }
    public static RegistrationResult failure(String message) {
        return new RegistrationResult(
                false,
                message,
                null,
                RegistrationStatus.FAILURE
        );
    }


    public static RegistrationResult alreadyRegistered(String message) {
        return RegistrationResult.builder()
                .success(false)
                .message(message)
                .registrationStatus(RegistrationStatus.ALREADY_REGISTERED)
                .build();
    }
}
