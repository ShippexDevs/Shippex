package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.auth.LoginRequest;
import com.shippex.dto.auth.LoginResponse;
import com.shippex.service.AuthService;
import com.shippex.dto.otp.PhoneOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for username={}", request.getUsername());
        try {

            LoginResponse response =
                    authService.login(request);
            log.info("Login successful for username={}", request.getUsername());
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Login successful.",
                            response
                    )
            );

        } catch (DisabledException ex) {
            log.warn("Login failed for disabled account username={}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Your account is disabled. Please contact support."));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for username={}. Invalid credentials.", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            ApiResponse.failure(
                                    "Invalid username or password."
                            )
                    );
        }
    }

    @PostMapping("/verify-otp-to-login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithWhatsappOtp(
            @Valid @RequestBody PhoneOtpRequest request) {
        try {
            LoginResponse response = authService.loginWithWhatsappOtp(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("Your account is disabled. Please contact support."));
        }
    }
}
