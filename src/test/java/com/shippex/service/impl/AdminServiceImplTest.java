package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.dto.admin.AdminLoginRequest;
import com.shippex.dto.admin.CreateAdminRequest;
import com.shippex.dto.admin.CreateAdminResponse;
import com.shippex.exception.DuplicateEmailException;
import com.shippex.exception.DuplicateUsernameException;
import com.shippex.model.AdminUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
import com.shippex.security.JwtService;
import com.shippex.service.EmailService;
import com.shippex.util.AdminMapper;
import com.shippex.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {
    @Mock AdminUserRepository adminUserRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PasswordGenerator passwordGenerator;
    @Mock AdminMapper adminMapper;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;
    @InjectMocks AdminServiceImpl service;

    private CreateAdminRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = CreateAdminRequest.builder().name("Ada").username("ada")
                .email("ada@example.com").whatsappContactNo("+15550000000").build();
    }

    @Test
    void createAdmin_persistsAdminAndSendsCredentials() {
        when(passwordGenerator.generateTemporaryPassword()).thenReturn("Temp123!");
        when(passwordEncoder.encode("Temp123!")).thenReturn("encoded");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminMapper.toCreateAdminResponse(any())).thenReturn(CreateAdminResponse.builder().username("ada").build());

        CreateAdminResponse response = service.createAdmin(createRequest);

        assertEquals("Admin created successfully. Credentials have been sent to the registered email.", response.getMessage());
        verify(adminUserRepository).save(argThat(admin -> admin.getRole() == Role.ADMIN
                && admin.getAccountStatus() == AccountStatus.ACTIVE
                && admin.getWhatsappContactNo().equals("+15550000000")));
        verify(emailService).sendAdminCredentials("ada@example.com", "Ada", "ada", "Temp123!");
    }

    @Test
    void createAdmin_rejectsUsernameUsedByAdmin() {
        when(adminUserRepository.existsByUsername("ada")).thenReturn(true);
        assertThrows(DuplicateUsernameException.class, () -> service.createAdmin(createRequest));
        verify(adminUserRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void createAdmin_rejectsUsernameUsedByAppUser() {
        when(appUserRepository.existsByUsername("ada")).thenReturn(true);
        assertThrows(DuplicateUsernameException.class, () -> service.createAdmin(createRequest));
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void createAdmin_rejectsDuplicateEmail() {
        when(adminUserRepository.existsByEmail("ada@example.com")).thenReturn(true);
        assertThrows(DuplicateEmailException.class, () -> service.createAdmin(createRequest));
        verify(adminUserRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void login_returnsTokenAndUpdatesLastLoginForActiveAdmin() {
        AdminUser admin = admin(AccountStatus.ACTIVE);
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt");
        AdminLoginRequest request = loginRequest("ada", "secret");

        var result = service.login(request);

        assertEquals("jwt", result.getAccessToken());
        assertEquals("ADMIN", result.getRole());
        assertNotNull(admin.getLastLoginAt());
        verify(adminUserRepository).save(admin);
    }

    @Test
    void login_hidesUnknownUsernameBehindAdminNotFound() {
        when(adminUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(com.shippex.exception.AdminNotFoundException.class,
                () -> service.login(loginRequest("unknown", "secret")));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_rejectsIncorrectPassword() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin(AccountStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> service.login(loginRequest("ada", "wrong")));
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void login_rejectsNonActiveAdminAfterPasswordVerification() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin(AccountStatus.DISABLED)));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.login(loginRequest("ada", "secret")));
        verify(adminUserRepository, never()).save(any());
    }

    private AdminUser admin(AccountStatus status) {
        AdminUser admin = new AdminUser("Ada", "ada", "encoded", "ada@example.com", Role.ADMIN, status);
        admin.setId("admin-1");
        admin.setFirstLogin(false);
        return admin;
    }

    private AdminLoginRequest loginRequest(String username, String password) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
