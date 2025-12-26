package com.bronx.notification.controller;

import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.telegramSender.TelegramMessageRequest;
import com.bronx.notification.service.NotificationService;
import com.bronx.notification.service.ValidateHeaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification")
@Slf4j
@RequiredArgsConstructor
public class NotificationApiController {

  private final NotificationService notificationService;
  private final ValidateHeaderService validateHeaderService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Object>> sendNotification(
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader("X-Secret-Key") String secretKey,
            @RequestBody TelegramMessageRequest request) {

        validateHeaderService.validate(clientId, secretKey);
        notificationService.createAndQueueNotification( request);
        return ResponseEntity.ok(ApiResponse.success(
                "notification was send to queue"
        ));
    }

}
