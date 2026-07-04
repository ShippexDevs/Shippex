package com.shippex.service.impl;

import com.shippex.constants.Designation;
import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.exception.OtpException;
import com.shippex.model.AppUser;
import com.shippex.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    private RegisterAppUserRequest request;

    @BeforeEach
    void setup() {

        request = new RegisterAppUserRequest();

        request.setName("John Doe");
        request.setUsername("john123");
        request.setPassword("password");

        request.setEmail("john@test.com");
        request.setWhatsappContactNo("+919876543210");

        request.setDesignation(Designation.MASTER);
        request.setShipName("Evergreen");
        request.setShipIMONumber("1234567");
    }

    @Test
    void createAppUserDetails_shouldCreateUser_whenOtpVerified() {

        when(otpService.isOtpVerified(request.getWhatsappContactNo()))
                .thenReturn(true);

        AppUser savedUser = new AppUser();

        savedUser.setName(request.getName());
        savedUser.setUsername(request.getUsername());
        savedUser.setEmail(request.getEmail());
        savedUser.setWhatsappContactNo(request.getWhatsappContactNo());
        savedUser.setDesignation(request.getDesignation());
        savedUser.setShipName(request.getShipName());
        savedUser.setShipIMONumber(request.getShipIMONumber());

        when(appUserRepository.save(any(AppUser.class)))
                .thenReturn(savedUser);

        RegisterAppUserResponse response =
                appUserService.createAppUserDetails(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getWhatsappContactNo()).isEqualTo(request.getWhatsappContactNo());
        assertThat(response.getDesignation()).isEqualTo(request.getDesignation());
        assertThat(response.getShipName()).isEqualTo(request.getShipName());
        assertThat(response.getShipIMONumber()).isEqualTo(request.getShipIMONumber());
        assertThat(response.getMessage()).isEqualTo("AppUser registered successfully.");

        verify(otpService).isOtpVerified(request.getWhatsappContactNo());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void createAppUserDetails_shouldSetVerifiedTrueBeforeSaving() {

        when(otpService.isOtpVerified(anyString()))
                .thenReturn(true);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.createAppUserDetails(request);

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(captor.capture());

        AppUser saved = captor.getValue();

        assertThat(saved.getVerified()).isTrue();
    }

    @Test
    void createAppUserDetails_shouldPopulateAllFields() {

        when(otpService.isOtpVerified(anyString()))
                .thenReturn(true);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.createAppUserDetails(request);

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(captor.capture());

        AppUser user = captor.getValue();

        assertThat(user.getName()).isEqualTo(request.getName());
        assertThat(user.getUsername()).isEqualTo(request.getUsername());
        assertThat(user.getPassword()).isEqualTo(request.getPassword());
        assertThat(user.getEmail()).isEqualTo(request.getEmail());
        assertThat(user.getWhatsappContactNo()).isEqualTo(request.getWhatsappContactNo());
        assertThat(user.getDesignation()).isEqualTo(request.getDesignation());
        assertThat(user.getShipName()).isEqualTo(request.getShipName());
        assertThat(user.getShipIMONumber()).isEqualTo(request.getShipIMONumber());

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastUpdatedAt()).isNotNull();
    }

    @Test
    void createAppUserDetails_shouldThrowOtpException_whenOtpNotVerified() {

        when(otpService.isOtpVerified(anyString()))
                .thenReturn(false);

        assertThatThrownBy(() ->
                appUserService.createAppUserDetails(request))
                .isInstanceOf(OtpException.class)
                .hasMessage("Phone number is not verified.");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void createAppUserDetails_shouldThrowException_whenRepositorySaveFails() {

        when(otpService.isOtpVerified(anyString()))
                .thenReturn(true);

        RuntimeException exception = new RuntimeException("Database down");

        when(appUserRepository.save(any(AppUser.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> appUserService.createAppUserDetails(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database down");

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void isUsernameAvailable_shouldReturnTrue_whenUserDoesNotExist() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.empty());

        boolean available =
                appUserService.isUsernameAvailable("john123");

        assertThat(available).isTrue();
    }

    @Test
    void isUsernameAvailable_shouldReturnFalse_whenUserExists() {

        when(appUserRepository.findByUsername("john123"))
                .thenReturn(Optional.of(new AppUser()));

        boolean available =
                appUserService.isUsernameAvailable("john123");

        assertThat(available).isFalse();
    }
}