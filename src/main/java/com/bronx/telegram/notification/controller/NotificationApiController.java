package com.bronx.telegram.notification.controller;
import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.checkIn.ChannelRequest;
import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import com.bronx.telegram.notification.model.entity.NotificationChannel;
import com.bronx.telegram.notification.model.entity.NotificationPersonal;
import com.bronx.telegram.notification.service.NotificationMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@Slf4j
@RequiredArgsConstructor
public class NotificationApiController {

  private final NotificationMapService  notificationMapService;

    @PostMapping("/personal/employee-code")
    public ResponseEntity<ApiResponse<Object>> sendPersonalNotification(
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader("X-Secret-Key") String secretKey,
            @RequestBody CheckInRequest request) {

        NotificationPersonal notification= notificationMapService.mapPersonalNotification(clientId, secretKey, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Personal notification queued",
                Map.of("notificationId", notification.getId())
        ));
    }

    @PostMapping("/notification/channel")
    public ResponseEntity<ApiResponse<Object>> sendChannelNotification(
            @RequestHeader("X-Client-Id") String clientId,
            @RequestHeader("X-Secret-Key") String secretKey,
            @RequestBody ChannelRequest request) {

        NotificationChannel notification = notificationMapService.mapChannelNotification(clientId, secretKey, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Personal notification queued",
                Map.of("notificationId", notification.getId())
        ));
    }

}
