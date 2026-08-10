package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.admin.ChangePasswordRequest;
import com.shippex.dto.admin.ChangePasswordResponse;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentAdmin
    ) {

        log.info(
                "Password change request received for admin={}",
                currentAdmin.getUsername()
        );

        ChangePasswordResponse response =
                adminAuthService.changePassword(
                        request,
                        currentAdmin
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response.getMessage(),
                        response
                )
        );
    }
}