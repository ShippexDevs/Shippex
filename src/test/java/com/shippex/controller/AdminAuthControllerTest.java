package com.shippex.controller;

import com.shippex.dto.admin.ChangePasswordRequest;
import com.shippex.dto.admin.ChangePasswordResponse;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AdminAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {
    @Mock AdminAuthService adminAuthService;
    @InjectMocks AdminAuthController controller;

    @Test
    void changePasswordReturnsServiceResponse() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        CustomUserDetails admin = mock(CustomUserDetails.class);
        ChangePasswordResponse result = ChangePasswordResponse.builder().message("changed").build();
        when(adminAuthService.changePassword(request, admin)).thenReturn(result);
        var response = controller.changePassword(request, admin);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("changed", response.getBody().getMessage());
        assertSame(result, response.getBody().getData());
    }

    @Test
    void changePasswordPropagatesServiceValidationFailures() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        CustomUserDetails admin = mock(CustomUserDetails.class);
        when(adminAuthService.changePassword(request, admin)).thenThrow(new IllegalArgumentException("invalid password"));
        assertThrows(IllegalArgumentException.class, () -> controller.changePassword(request, admin));
    }
}
