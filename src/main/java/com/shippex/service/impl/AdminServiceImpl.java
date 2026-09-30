package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.dto.admin.*;
import com.shippex.dto.auth.CurrentUserResponse;
import com.shippex.exception.AdminNotFoundException;
import com.shippex.exception.DuplicateEmailException;
import com.shippex.exception.DuplicateUsernameException;
import com.shippex.model.AdminUser;
import com.shippex.model.AppUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    private final AdminUserRepository adminUserRepository;
    private final AppUserRepository appUserRepository;
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
        admin.setWhatsappContactNo(request.getWhatsappContactNo());

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

        log.debug(
                "Checking username availability across user types."
        );

        boolean adminExists =
                adminUserRepository.existsByUsername(username);

        boolean appUserExists =
                appUserRepository.existsByUsername(username);

        if (adminExists || appUserExists) {
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
                .role(admin.getRole().name().replace('_', ' '))
                .firstLogin(admin.getFirstLogin())
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrentUserResponse> getAllAppUsers() {
        log.debug("Fetching all App Users for admin");
        return appUserRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CurrentUserResponse updateAppUserStatus(String userId, boolean enabled) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "App User not found with ID: " + userId
                        )
                );

        user.setAccountStatus(
                enabled
                        ? AccountStatus.ACTIVE
                        : AccountStatus.DISABLED
        );

        AppUser updatedUser = appUserRepository.save(user);

        log.info(
                "App User {} has been {}",
                userId,
                enabled ? "enabled" : "disabled"
        );

        return toResponse(updatedUser);
    }

    private CurrentUserResponse toResponse(AppUser user) {
        return CurrentUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .whatsappContactNo(user.getWhatsappContactNo())
                .designation(user.getDesignation())
                .shipName(user.getShipName())
                .shipIMONumber(user.getShipIMONumber())
                .verified(user.getVerified())
                .accountStatus(
                        user.getAccountStatus() != null
                                ? user.getAccountStatus().toString()
                                : null
                )
                .build();
    }
}
