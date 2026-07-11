package com.shippex.service.impl;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

//✅ Successful login.
//✅ AuthenticationManager is invoked.
//✅ Correct username/password are passed.
//✅ JWT is generated.
//✅ Correct LoginResponse is returned.
//✅ Invalid credentials throw BadCredentialsException.
//✅ JWT is not generated if authentication fails.

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest request;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {

        request = new LoginRequest();
        request.setUsername("john123");
        request.setPassword("password");

        AppUser user = new AppUser();
        user.setId("user-id");
        user.setName("John Doe");
        user.setUsername("john123");
        user.setPassword("$2a$10$hashedPassword");
        user.setVerified(true);

        userDetails = new CustomUserDetails(user);
    }

    @Test
    void login_shouldAuthenticateUserSuccessfully() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertThat(response).isNotNull();

        assertThat(response.getAccessToken())
                .isEqualTo("jwt-token");

        assertThat(response.getTokenType())
                .isEqualTo("Bearer");

        assertThat(response.getUsername())
                .isEqualTo("john123");

        assertThat(response.getName())
                .isEqualTo("John Doe");

        verify(authenticationManager)
                .authenticate(any());

        verify(jwtService)
                .generateToken(userDetails);
    }

    @Test
    void login_shouldPassCorrectCredentialsToAuthenticationManager() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager)
                .authenticate(captor.capture());

        UsernamePasswordAuthenticationToken token =
                captor.getValue();

        assertThat(token.getPrincipal())
                .isEqualTo("john123");

        assertThat(token.getCredentials())
                .isEqualTo("password");
    }

    @Test
    void login_shouldGenerateJwtToken() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn("generated-token");

        LoginResponse response =
                authService.login(request);

        assertThat(response.getAccessToken())
                .isEqualTo("generated-token");

        verify(jwtService)
                .generateToken(userDetails);
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"
                        )
                );

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void login_shouldReturnCorrectUserInformation() {

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(any()))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertThat(response.getUsername())
                .isEqualTo(userDetails.getUsername());

        assertThat(response.getName())
                .isEqualTo(userDetails.getName());

        assertThat(response.getTokenType())
                .isEqualTo("Bearer");
    }
}