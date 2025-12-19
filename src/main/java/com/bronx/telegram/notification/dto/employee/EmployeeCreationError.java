package com.bronx.telegram.notification.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeCreationError {
    private final int index;
    private final String email;
    private final String errorMessage;
}
