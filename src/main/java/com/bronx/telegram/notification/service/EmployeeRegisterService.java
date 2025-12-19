package com.bronx.telegram.notification.service;

import com.bronx.telegram.notification.dto.employee.EmployeeRequest;
import com.bronx.telegram.notification.dto.employee.RegistrationResult;
import com.bronx.telegram.notification.model.entity.Employee;

import java.util.List;

public interface EmployeeRegisterService {
    RegistrationResult registerEmployee(String email, String telegramUserId,
                                        String telegramUsername, String chatId);
    List<Employee> batchCreateEmployee(List<EmployeeRequest> employeeRequests);
}
