package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.UpdateAppUserFieldRequest;
import com.shippex.dto.UpdateEmailRequest;
import com.shippex.dto.ChangePasswordRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.dto.auth.CurrentUserResponse;
import com.shippex.model.AppUser;
import com.shippex.repository.AppUserRepository;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.shippex.constants.Designation;

@RestController
@RequestMapping("/api/appUser")
@Slf4j
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService, AppUserRepository appUserRepository) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        AppUser user = appUserService.getByUsername(userDetails.getUsername());

        CurrentUserResponse response =
                CurrentUserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .whatsappContactNo(user.getWhatsappContactNo())
                        .designation(user.getDesignation())
                        .shipName(user.getShipName())
                        .shipIMONumber(user.getShipIMONumber())
                        .verified(user.getVerified())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User profile retrieved successfully.",
                        response
                )
        );
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthenticationPrincipal CustomUserDetails user,
                                                             @Valid @RequestBody ChangePasswordRequest request) {
        appUserService.updatePassword(user.getUsername(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully."));
    }

    @PatchMapping("/me/whatsapp-contact-no")
    public ResponseEntity<ApiResponse<Void>> updateWhatsappContactNo(@AuthenticationPrincipal CustomUserDetails user,
                                                                      @Valid @RequestBody VerifyOtpRequest request) {
        appUserService.verifyAndUpdateWhatsappContactNo(user.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("WhatsApp contact number verified and updated successfully."));
    }

    @PatchMapping("/me/name")
    public ResponseEntity<ApiResponse<Void>> updateName(@AuthenticationPrincipal CustomUserDetails user,
                                                        @Valid @RequestBody UpdateAppUserFieldRequest request) {
        appUserService.updateName(user.getUsername(), request.value());
        return ResponseEntity.ok(ApiResponse.success("Name updated successfully."));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<ApiResponse<Void>> updateEmail(@AuthenticationPrincipal CustomUserDetails user,
                                                         @Valid @RequestBody UpdateEmailRequest request) {
        appUserService.updateEmail(user.getUsername(), request.email());
        return ResponseEntity.ok(ApiResponse.success("Email updated successfully."));
    }

    @PatchMapping("/me/designation")
    public ResponseEntity<ApiResponse<Void>> updateDesignation(@AuthenticationPrincipal CustomUserDetails user,
                                                               @Valid @RequestBody UpdateAppUserFieldRequest request) {
        try {
            appUserService.updateDesignation(user.getUsername(), Designation.valueOf(request.value().trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Designation is invalid.");
        }
        return ResponseEntity.ok(ApiResponse.success("Designation updated successfully."));
    }

    @PatchMapping("/me/ship-name")
    public ResponseEntity<ApiResponse<Void>> updateShipName(@AuthenticationPrincipal CustomUserDetails user,
                                                            @Valid @RequestBody UpdateAppUserFieldRequest request) {
        appUserService.updateShipName(user.getUsername(), request.value());
        return ResponseEntity.ok(ApiResponse.success("Ship name updated successfully."));
    }

    @PatchMapping("/me/ship-imo-number")
    public ResponseEntity<ApiResponse<Void>> updateShipIMONumber(@AuthenticationPrincipal CustomUserDetails user,
                                                                 @Valid @RequestBody UpdateAppUserFieldRequest request) {
        appUserService.updateShipIMONumber(user.getUsername(), request.value());
        return ResponseEntity.ok(ApiResponse.success("Ship IMO number updated successfully."));
    }
}