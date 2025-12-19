package com.bronx.telegram.notification.controller;
import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.employee.EmployeeRequest;
import com.bronx.telegram.notification.dto.employee.EmployeeResponse;
import com.bronx.telegram.notification.service.impl.EmployeeRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeRegistrationService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmployeeResponse> createDivision(
            @Valid @RequestBody EmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ApiResponse.success("Division created successfully", response);
    }

//    @PostMapping("/notification/channel")
//    public ResponseEntity<ApiResponse<Object>> sendChannelNotification(
//            @RequestHeader("X-Client-Id") String clientId,
//            @RequestHeader("X-Secret-Key") String secretKey,
//            @RequestBody ChannelRequest request) {
//
//        NotificationChannel notification = notificationMapService.mapChannelNotification(clientId, secretKey, request);
//        return ResponseEntity.ok(ApiResponse.success(
//                "Personal notification queued",
//                Map.of("notificationId", notification.getId())
//        ));
//    }

}
