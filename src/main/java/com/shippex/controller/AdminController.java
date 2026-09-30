package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.admin.*;
import com.shippex.dto.auth.CurrentUserResponse;
import com.shippex.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CreateAdminResponse>> createAdmin(
            @Valid @RequestBody CreateAdminRequest request
    ) {
        log.info("Received request to create admin with username={}", request.getUsername());

        CreateAdminResponse response = adminService.createAdmin(request);

        log.info("Admin created successfully with username={}", response.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Admin created successfully.",
                        response
                ));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {

        log.info("Fetching all admins.");

        List<AdminResponse> response = adminService.getAllAdmins();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admins retrieved successfully.",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(
            @PathVariable String id) {

        log.info("Fetching admin with id={}", id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin retrieved successfully.",
                        adminService.getAdminById(id)
                )
        );
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @PathVariable String id,
            @Valid @RequestBody UpdateAdminRequest request) {

        log.info("Updating admin with id={}", id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Admin updated successfully.",
                        adminService.updateAdmin(id, request)
                )
        );
    }

    @PatchMapping("/{id}/enable")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> enableAdmin(
            @PathVariable String id) {

        log.info("Enabling admin with id={}", id);

        adminService.enableAdmin(id);

        return ResponseEntity.ok(
                ApiResponse.success("Admin enabled successfully.")
        );
    }

    @PatchMapping("/{id}/disable")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> disableAdmin(
            @PathVariable String id) {

        log.info("Disabling admin with id={}", id);

        adminService.disableAdmin(id);

        return ResponseEntity.ok(
                ApiResponse.success("Admin disabled successfully.")
        );
    }
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @Valid
            @RequestBody
            AdminLoginRequest request
    ) {

        return ResponseEntity.ok(
                adminService.login(request)
        );
    }
    @GetMapping("/users")
    public ApiResponse<List<CurrentUserResponse>> getAllUsers() {
        return ApiResponse.success(
                "AppUsers retrieved successfully.",
                adminService.getAllAppUsers()
        );
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<CurrentUserResponse> updateAppUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateAppUserStatusRequest request
    ) {
        CurrentUserResponse response =
                adminService.updateAppUserStatus(
                        userId,
                        request.getEnabled()
                );

        return ApiResponse.success("Status changed for AppUser.",response);
    }
}
