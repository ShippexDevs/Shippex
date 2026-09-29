package com.shippex.controller;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthService authService;
    @InjectMocks AuthController controller;

    @Test
    void login_returnsSuccessfulAuthenticationResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("buyer");
        request.setPassword("secret");
        LoginResponse loginResponse = LoginResponse.builder().accessToken("jwt").build();
        when(authService.login(request)).thenReturn(loginResponse);

        var response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertSame(loginResponse, response.getBody().getData());
    }

    @Test
    void login_returnsUnauthorizedForBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("buyer");
        request.setPassword("wrong");
        when(authService.login(request)).thenThrow(new BadCredentialsException("bad credentials"));

        var response = controller.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password.", response.getBody().getMessage());
    }
}
