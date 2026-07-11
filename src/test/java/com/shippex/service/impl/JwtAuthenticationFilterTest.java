package com.shippex.service.impl;

import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtAuthenticationFilter;
import com.shippex.security.JwtService;
import com.shippex.service.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private CustomUserDetails user;

    @BeforeEach
    void setUp() {

        request = new MockHttpServletRequest();

        response = new MockHttpServletResponse();

        AppUser appUser = new AppUser();

        appUser.setId("user-1");
        appUser.setName("John Doe");
        appUser.setUsername("john123");
        appUser.setPassword("password");
        appUser.setVerified(true);

        user = new CustomUserDetails(appUser);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilter_whenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);

        verifyNoInteractions(userDetailsService);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void shouldContinueFilter_whenHeaderDoesNotStartWithBearer()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);

        verifyNoInteractions(userDetailsService);

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }

    @Test
    void shouldAuthenticateUser_whenJwtIsValid()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("john123");

        when(userDetailsService.loadUserByUsername("john123"))
                .thenReturn(user);

        when(jwtService.isTokenValid(
                "valid-token",
                user
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNotNull();

        verify(jwtService)
                .extractUsername("valid-token");

        verify(jwtService)
                .isTokenValid("valid-token", user);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenJwtIsInvalid()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("john123");

        when(userDetailsService.loadUserByUsername("john123"))
                .thenReturn(user);

        when(jwtService.isTokenValid(
                "invalid-token",
                user
        )).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldSkipAuthentication_whenAlreadyAuthenticated()
            throws ServletException, IOException {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        mock(org.springframework.security.core.Authentication.class)
                );

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("john123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService)
                .extractUsername("valid-token");

        verify(userDetailsService, never())
                .loadUserByUsername(any());

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueFilter_whenUsernameIsNull()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer token"
        );

        when(jwtService.extractUsername("token"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verify(userDetailsService, never())
                .loadUserByUsername(any());

        assertThat(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        ).isNull();
    }
}
