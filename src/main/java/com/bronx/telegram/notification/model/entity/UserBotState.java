package com.bronx.telegram.notification.model.entity;

import com.bronx.telegram.notification.model.enumz.RegistrationState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_bot_states")
public class UserBotState {
    @Id
    private String chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private RegistrationState state;
    private String role;
    private String fullName;
    private String companyCode;
    private String email;
    private String contact;
    private String employeeCode;
    private Instant lastModified;
}
