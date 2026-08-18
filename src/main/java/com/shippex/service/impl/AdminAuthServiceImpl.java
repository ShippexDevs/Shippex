package com.shippex.service.impl;

import com.shippex.dto.admin.ChangePasswordRequest;
import com.shippex.dto.admin.ChangePasswordResponse;
import com.shippex.exception.AdminNotFoundException;
import com.shippex.exception.InvalidCurrentPasswordException;
import com.shippex.exception.PasswordMismatchException;
import com.shippex.exception.SamePasswordException;
import com.shippex.model.AdminUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ChangePasswordResponse changePassword(
            ChangePasswordRequest request,
            CustomUserDetails currentAdmin
    ) {

        log.info(
                "Password change requested by admin={}",
                currentAdmin.getUsername()
        );

        AdminUser admin = adminUserRepository
                .findByUsername(currentAdmin.getUsername())
                .orElseThrow(() -> {

                    log.error(
                            "Admin not found. username={}",
                            currentAdmin.getUsername()
                    );

                    return new AdminNotFoundException(
                            "Admin not found."
                    );

                });

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                admin.getPassword()
        )) {

            log.warn(
                    "Invalid current password supplied by admin={}",
                    admin.getUsername()
            );

            throw new InvalidCurrentPasswordException();

        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            log.warn(
                    "Password confirmation mismatch for admin={}",
                    admin.getUsername()
            );

            throw new PasswordMismatchException();

        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                admin.getPassword()
        )) {

            log.warn(
                    "Admin {} attempted to reuse existing password.",
                    admin.getUsername()
            );

            throw new SamePasswordException();

        }

        admin.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        admin.setFirstLogin(false);

        admin.setLastUpdatedAt(LocalDateTime.now());

        adminUserRepository.save(admin);

        log.info(
                "Password updated successfully for admin={}",
                admin.getUsername()
        );

        return ChangePasswordResponse.builder()
                .message("Password changed successfully.")
                .build();

    }

}
