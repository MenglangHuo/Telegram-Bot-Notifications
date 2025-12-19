package com.bronx.telegram.notification.dto.employee;

import com.bronx.telegram.notification.model.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BatchEmployeeResult {
    private final List<Employee> successfulEmployees;
    private final List<EmployeeCreationError> errors;
    private final int successCount;
    private final int errorCount;

    public boolean hasErrors() {
        return errorCount > 0;
    }

    public boolean isPartialSuccess() {
        return successCount > 0 && errorCount > 0;
    }

    public boolean isCompleteSuccess() {
        return successCount > 0 && errorCount == 0;
    }

    public boolean isCompleteFailure() {
        return successCount == 0 && errorCount > 0;
    }
}
