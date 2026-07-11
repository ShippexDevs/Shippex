package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.auth.CurrentUserResponse;
import com.shippex.model.AppUser;
import com.shippex.repository.AppUserRepository;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}