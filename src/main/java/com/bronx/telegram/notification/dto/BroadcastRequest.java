package com.bronx.telegram.notification.dto;

import com.bronx.telegram.notification.dto.checkIn.CheckInRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastRequest {
    private List<Long> channelIds;
    private List<String> employeeIds;
    private CheckInRequest notification;
}
