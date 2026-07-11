package com.shippex.service.impl;

import com.shippex.constants.Designation;
import com.shippex.controller.AppUserController;
import com.shippex.dto.ApiResponse;
import com.shippex.dto.auth.CurrentUserResponse;
import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private AppUserController controller;

    private AppUser appUser;

    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {

        appUser = new AppUser();

        appUser.setId("user-1");
        appUser.setName("John Doe");
        appUser.setUsername("john123");
        appUser.setEmail("john@test.com");
        appUser.setWhatsappContactNo("+919876543210");
        appUser.setDesignation(Designation.MASTER);
        appUser.setShipName("Evergreen");
        appUser.setShipIMONumber("1234567");
        appUser.setVerified(true);

        principal = new CustomUserDetails(appUser);
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUser() {

        when(appUserService.getByUsername("john123"))
                .thenReturn(appUser);

        ResponseEntity<ApiResponse<CurrentUserResponse>> response =
                controller.getCurrentUser(principal);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().isSuccess()).isTrue();

        CurrentUserResponse data =
                response.getBody().getData();

        assertThat(data.getId()).isEqualTo("user-1");
        assertThat(data.getName()).isEqualTo("John Doe");
        assertThat(data.getUsername()).isEqualTo("john123");
        assertThat(data.getEmail()).isEqualTo("john@test.com");
        assertThat(data.getWhatsappContactNo()).isEqualTo("+919876543210");
        assertThat(data.getDesignation()).isEqualTo(Designation.MASTER);
        assertThat(data.getShipName()).isEqualTo("Evergreen");
        assertThat(data.getShipIMONumber()).isEqualTo("1234567");
        assertThat(data.getVerified()).isTrue();

        verify(appUserService)
                .getByUsername("john123");
    }

    @Test
    void getCurrentUser_shouldReturnVerifiedFalse() {

        appUser.setVerified(false);

        when(appUserService.getByUsername("john123"))
                .thenReturn(appUser);

        ResponseEntity<ApiResponse<CurrentUserResponse>> response =
                controller.getCurrentUser(principal);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody()
                .getData()
                .getVerified())
                .isFalse();
    }

    @Test
    void getCurrentUser_shouldCallServiceOnce() {

        when(appUserService.getByUsername("john123"))
                .thenReturn(appUser);

        controller.getCurrentUser(principal);

        verify(appUserService, times(1))
                .getByUsername("john123");
    }
}