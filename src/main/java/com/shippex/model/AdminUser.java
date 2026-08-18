package com.shippex.model;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "admin_user")
@NoArgsConstructor
public class AdminUser extends BaseUser{
    public AdminUser(String name, String username, String password) {
        super(name, username, password);
    }
    public AdminUser(String name, String username, String password, String email) {
        super(name, username, password, email);
    }


    private Boolean mfaEnabled;
    private Boolean firstLogin;
    private LocalDateTime lastLoginAt;

    public AdminUser(String name,
                     String username,
                     String password,
                     String email,
                     Role role,
                     AccountStatus accountStatus) {

        super(name, username, password, email);

        this.setRole(role);
        this.setAccountStatus(accountStatus);
        this.firstLogin = true;
        this.mfaEnabled = false;
    }
}
