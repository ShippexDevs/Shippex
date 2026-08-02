package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.dto.admin.*;
import com.shippex.exception.AdminNotFoundException;
import com.shippex.exception.DuplicateEmailException;
import com.shippex.exception.DuplicateUsernameException;
import com.shippex.model.AdminUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtService;
import com.shippex.service.AdminService;
import com.shippex.service.EmailService;
import com.shippex.util.AdminMapper;
import com.shippex.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final AdminMapper adminMapper;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public CreateAdminResponse createAdmin(CreateAdminRequest request) {
        log.info("Creating admin with username={}", request.getUsername());

        validateUsername(request.getUsername());
        validateEmail(request.getEmail());

        String temporaryPassword = passwordGenerator.generateTemporaryPassword();

        AdminUser admin = new AdminUser(
                request.getName(),
                request.getUsername(),
                passwordEncoder.encode(temporaryPassword),
                request.getEmail(),
                Role.ADMIN,
                AccountStatus.ACTIVE
        );

        AdminUser savedAdmin = adminUserRepository.save(admin);

        emailService.sendAdminCredentials(
                savedAdmin.getEmail(),
                savedAdmin.getName(),
                savedAdmin.getUsername(),
                temporaryPassword
        );

        log.info("Admin created successfully. Username={}", savedAdmin.getUsername());

        CreateAdminResponse response = adminMapper.toCreateAdminResponse(savedAdmin);
        response.setMessage(
                "Admin created successfully. Credentials have been sent to the registered email."
        );


        return response;
    }

    private void validateUsername(String username) {

        log.debug("Checking username availability.");

        if (adminUserRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void validateEmail(String email) {

        log.debug("Checking email availability.");

        if (adminUserRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    @Override
    public List<AdminResponse> getAllAdmins() {
        return List.of();
    }

    @Override
    public AdminResponse getAdminById(String id) {
        return null;
    }

    @Override
    public AdminResponse updateAdmin(String id, UpdateAdminRequest request) {
        return null;
    }

    @Override
    public void enableAdmin(String id) {

    }

    @Override
    public void disableAdmin(String id) {

    }

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {

        log.info("Admin login attempt for username: {}", request.getUsername());

        AdminUser admin = adminUserRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new AdminNotFoundException(
                                "Invalid username or password."
                        ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPassword()
        )) {

            log.warn("Invalid password for {}", request.getUsername());

            throw new BadCredentialsException(
                    "Invalid username or password."
            );
        }

        if (admin.getAccountStatus() != AccountStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Admin account is disabled."
            );
        }

        admin.setLastLoginAt(LocalDateTime.now());

        adminUserRepository.save(admin);

        CustomUserDetails userDetails = new CustomUserDetails(admin);
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("Admin {} logged in successfully", admin.getUsername());

        return AdminLoginResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .username(admin.getUsername())
                .role(admin.getRole().name())
                .firstLogin(admin.getFirstLogin())
                .build();

    }
}
