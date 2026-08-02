package com.shippex.bootstarp;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.model.AdminUser;
import com.shippex.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminBootstrap implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminProperties properties;

    @Override
    public void run(String... args) {
        if (adminUserRepository.existsByUsername(properties.getUsername())) {
            log.info("SUPER_ADMIN already exists. Skipping bootstrap.");
            return;
        }

        AdminUser superAdmin = new AdminUser(properties.getName(),
                properties.getUsername(),
                properties.getEmail());
        superAdmin.setPassword(passwordEncoder.encode(properties.getPassword()));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setAccountStatus(AccountStatus.ACTIVE);
        superAdmin.setFirstLogin(true);
        superAdmin.setMfaEnabled(true);

        adminUserRepository.save(superAdmin);

        log.info(
                "Default SUPER_ADMIN [{}] created successfully.",
                superAdmin.getUsername()
        );
    }
}
