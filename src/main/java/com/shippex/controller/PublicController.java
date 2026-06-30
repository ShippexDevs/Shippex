package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.exception.OtpException;
import com.shippex.service.AppUserService;
import com.shippex.service.impl.OtpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Slf4j
public class PublicController {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/checkUsername/{username}")
    public ResponseEntity<?> isUsernameAvailable(@PathVariable String username) {
        if(appUserService.isUsernameAvailable(username)) {
            return new ResponseEntity<>("Username available", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Username not available", HttpStatus.GONE);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> createNewAppUser(@RequestBody RegisterAppUserRequest userRequest) {

        log.debug("Hit /request endpoint for :: {}", userRequest.toString());
        RegisterAppUserResponse appUserDetails;
        try {
            appUserDetails = appUserService.createAppUserDetails(userRequest);
            log.debug("Returning /request endpoint for :: {}", userRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<RegisterAppUserResponse>builder()
                            .success(true)
                            .message("User registered successfully.")
                            .data(appUserDetails)
                            .build());
        } catch (OtpException e) {
            log.debug("Error encountered while creating :: {}, {}", userRequest, e.toString());
            return new ResponseEntity<>("WhatsApp number is not verified, hence register failed!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generate OTP
     */
    @PostMapping("/generateOtp")
    public ResponseEntity<ApiResponse<Void>> generateOtp(
            @Valid @RequestBody GenerateOtpRequest request) {

        log.info("Received OTP generation request for {}", request.getPhoneNumber());

        otpService.generateOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully.")
        );
    }

    /**
     * Verify OTP
     */
    @PostMapping("/verifyOtp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        log.info("Received OTP verification request for {}", request.getPhoneNumber());

        otpService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("OTP verified successfully.")
        );
    }
}
