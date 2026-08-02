package com.shippex.dto.admin;

import com.shippex.constants.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponse {
    private String id;
    private String name;
    private String username;
    private String email;
    private Boolean mfaEnabled;
    private Boolean firstLogin;
    private AccountStatus accountStatus;
    private LocalDateTime lastLoginAt;
}
