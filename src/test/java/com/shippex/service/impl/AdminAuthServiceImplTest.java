package com.shippex.service.impl;

import com.shippex.dto.admin.ChangePasswordRequest;
import com.shippex.exception.AdminNotFoundException;
import com.shippex.exception.InvalidCurrentPasswordException;
import com.shippex.exception.PasswordMismatchException;
import com.shippex.exception.SamePasswordException;
import com.shippex.model.AdminUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceImplTest {
    @Mock AdminUserRepository adminUserRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AdminAuthServiceImpl service;

    private AdminUser admin;
    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        admin = new AdminUser("Ada", "ada", "encoded-old", "ada@example.com");
        admin.setFirstLogin(true);
        principal = new CustomUserDetails(admin);
    }

    @Test
    void changePassword_updatesHashAndClearsFirstLogin() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.matches("new", "encoded-old")).thenReturn(false);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");

        var response = service.changePassword(request("old", "new", "new"), principal);

        assertEquals("Password changed successfully.", response.getMessage());
        assertEquals("encoded-new", admin.getPassword());
        assertFalse(admin.getFirstLogin());
        assertNotNull(admin.getLastUpdatedAt());
        verify(adminUserRepository).save(admin);
    }

    @Test
    void changePassword_throwsWhenAdminDoesNotExist() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.empty());
        assertThrows(AdminNotFoundException.class,
                () -> service.changePassword(request("old", "new", "new"), principal));
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void changePassword_rejectsWrongCurrentPassword() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);
        assertThrows(InvalidCurrentPasswordException.class,
                () -> service.changePassword(request("wrong", "new", "new"), principal));
        verify(passwordEncoder, never()).encode(any());
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void changePassword_rejectsMismatchedConfirmation() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        assertThrows(PasswordMismatchException.class,
                () -> service.changePassword(request("old", "new", "other"), principal));
        verify(adminUserRepository, never()).save(any());
    }

    @Test
    void changePassword_rejectsReusingCurrentPassword() {
        when(adminUserRepository.findByUsername("ada")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        assertThrows(SamePasswordException.class,
                () -> service.changePassword(request("old", "old", "old"), principal));
        verify(adminUserRepository, never()).save(any());
    }

    private ChangePasswordRequest request(String current, String next, String confirm) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(next);
        request.setConfirmPassword(confirm);
        return request;
    }
}
