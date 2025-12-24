package com.bronx.notification.dto.notification;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PersonalNotificationMessage extends NotificationMessage {

    private Long employeeId;
    private String telegramChatId;
    private String employeeEmail;
    private String employeeName;

}
