package com.shippex.controller;

import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.exception.OtpException;
import com.shippex.service.AppUserService;
import com.shippex.service.impl.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicControllerTest {
    @Mock AppUserService appUserService;
    @Mock OtpService otpService;
    private PublicController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PublicController();
        ReflectionTestUtils.setField(controller, "appUserService", appUserService);
        ReflectionTestUtils.setField(controller, "otpService", otpService);
    }

    @Test
    void checkUsernameReturnsAvailableAndUnavailableStatuses() {
        when(appUserService.isUsernameAvailable("new-user")).thenReturn(true);
        when(appUserService.isUsernameAvailable("used-user")).thenReturn(false);
        assertEquals(HttpStatus.OK, controller.isUsernameAvailable("new-user").getStatusCode());
        assertEquals(HttpStatus.GONE, controller.isUsernameAvailable("used-user").getStatusCode());
    }

    @Test
    void registerReturnsCreatedForVerifiedUser() {
        RegisterAppUserRequest request = new RegisterAppUserRequest();
        RegisterAppUserResponse result = RegisterAppUserResponse.builder().username("buyer").build();
        when(appUserService.createAppUserDetails(request)).thenReturn(result);
        var response = controller.createNewAppUser(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registerMapsOtpFailureToBadRequest() {
        RegisterAppUserRequest request = new RegisterAppUserRequest();
        when(appUserService.createAppUserDetails(request)).thenThrow(new OtpException("not verified"));
        var response = controller.createNewAppUser(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("WhatsApp number is not verified, hence register failed!", response.getBody());
    }

    @Test
    void registerMapsUnexpectedFailureToInternalServerError() {
        RegisterAppUserRequest request = new RegisterAppUserRequest();
        when(appUserService.createAppUserDetails(request)).thenThrow(new IllegalStateException("database down"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createNewAppUser(request).getStatusCode());
    }

    @Test
    void generateAndVerifyOtpDelegateRequests() {
        GenerateOtpRequest generate = new GenerateOtpRequest();
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest();
        assertEquals(HttpStatus.OK, controller.generateOtp(generate).getStatusCode());
        assertEquals(HttpStatus.OK, controller.verifyOtp(verifyRequest).getStatusCode());
        verify(otpService).generateOtp(generate);
        verify(otpService).verifyOtp(verifyRequest);
    }
}
