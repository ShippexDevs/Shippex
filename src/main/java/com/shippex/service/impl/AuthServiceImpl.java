package com.shippex.service.impl;

import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.security.CustomUserDetails;
import com.shippex.security.JwtService;
import com.shippex.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.debug("Authenticating user={}", request.getUsername());
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();
        log.info("Authentication successful for user={}", user.getUsername());
        String token = jwtService.generateToken(user);
        log.debug("JWT generated successfully for user={}", user.getUsername());
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }
}