package com.shippex.service.impl;

import com.shippex.constants.Designation;
import com.shippex.dto.RegisterAppUserRequest;
import com.shippex.dto.RegisterAppUserResponse;
import com.shippex.exception.OtpException;
import com.shippex.model.AppUser;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.exception.OtpException;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

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

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("$2a$encodedPassword");

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

        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$encodedPassword");

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

        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$encodedPassword");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.createAppUserDetails(request);

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(captor.capture());

        AppUser user = captor.getValue();

        assertThat(user.getName()).isEqualTo(request.getName());
        assertThat(user.getUsername()).isEqualTo(request.getUsername());
        assertThat(user.getPassword()).isEqualTo("$2a$encodedPassword");
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

        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$encodedPassword");

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

        when(appUserRepository.existsByUsername("john123"))
                .thenReturn(false);

        when(adminUserRepository.existsByUsername("john123"))
                .thenReturn(false);

        boolean available =
                appUserService.isUsernameAvailable("john123");

        assertThat(available).isTrue();
    }

    @Test
    void isUsernameAvailable_shouldReturnFalse_whenAppUserExists() {

        when(appUserRepository.existsByUsername("john123"))
                .thenReturn(true);

        when(adminUserRepository.existsByUsername("john123"))
                .thenReturn(false);

        boolean available =
                appUserService.isUsernameAvailable("john123");

        assertThat(available).isFalse();
    }

    @Test
    void updateProfileFields_shouldPersistEachChangedFieldAndRefreshTimestamp() {
        AppUser user = new AppUser();
        user.setUsername("john123");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.updateName("john123", "Jane Doe");
        appUserService.updateEmail("john123", "jane@example.com");
        appUserService.updateDesignation("john123", Designation.CHIEF_OFFICER);
        appUserService.updateShipName("john123", "Endeavour");
        appUserService.updateShipIMONumber("john123", "7654321");

        assertThat(user.getName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getDesignation()).isEqualTo(Designation.CHIEF_OFFICER);
        assertThat(user.getShipName()).isEqualTo("Endeavour");
        assertThat(user.getShipIMONumber()).isEqualTo("7654321");
        assertThat(user.getLastUpdatedAt()).isNotNull();
        verify(appUserRepository, times(5)).save(user);
    }

    @Test
    void updatePassword_shouldEncodeBeforePersisting() {
        AppUser user = new AppUser();
        user.setUsername("john123");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        user.setPassword("old-encoded");
        when(passwordEncoder.matches("OldPassword123!", "old-encoded")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(user);

        appUserService.updatePassword("john123", "OldPassword123!", "NewPassword123!");

        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(passwordEncoder).encode("NewPassword123!");
        verify(passwordEncoder).matches("OldPassword123!", "old-encoded");
        verify(appUserRepository).save(user);
    }

    @Test
    void updatePassword_shouldRejectIncorrectCurrentPasswordWithoutSaving() {
        AppUser user = new AppUser();
        user.setPassword("old-encoded");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-encoded")).thenReturn(false);

        assertThatThrownBy(() -> appUserService.updatePassword("john123", "wrong", "new"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect.");
        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void verifyAndUpdateWhatsappContactNo_shouldVerifyOtpBeforePersistingNumber() {
        AppUser user = new AppUser();
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("+12025550123");
        request.setOtp("123456");

        appUserService.verifyAndUpdateWhatsappContactNo("john123", request);

        assertThat(user.getWhatsappContactNo()).isEqualTo("+12025550123");
        verify(otpService).verifyOtp(request);
        verify(appUserRepository).save(user);
    }

    @Test
    void verifyAndUpdateWhatsappContactNo_shouldNotPersistWhenOtpVerificationFails() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+12025550000");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("+12025550123");
        request.setOtp("000000");
        doThrow(new OtpException("Invalid OTP.")).when(otpService).verifyOtp(request);

        assertThatThrownBy(() -> appUserService.verifyAndUpdateWhatsappContactNo("john123", request))
                .isInstanceOf(OtpException.class)
                .hasMessage("Invalid OTP.");
        assertThat(user.getWhatsappContactNo()).isEqualTo("+12025550000");
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void updateField_shouldFailWithoutPersistingWhenUserDoesNotExist() {
        when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.updateName("missing", "New Name"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found.");
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void updateField_shouldPropagatePersistenceFailure() {
        AppUser user = new AppUser();
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        when(appUserRepository.save(user)).thenThrow(new RuntimeException("Database down"));

        assertThatThrownBy(() -> appUserService.updateShipName("john123", "New Ship"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database down");
    }

    @Test
    void getMaskedWhatsappContactNo_masksEverythingExceptFirstAndLastTwoDigits() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));

        assertThat(appUserService.getMaskedWhatsappContactNo("john123")).isEqualTo("91********10");
    }

    @Test
    void getMaskedWhatsappContactNo_rejectsMissingNumber() {
        AppUser user = new AppUser();
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> appUserService.getMaskedWhatsappContactNo("john123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No WhatsApp number is associated with this account.");
    }

    @Test
    void generatePasswordResetOtp_sendsOtpToUsersStoredPhone() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));

        appUserService.generatePasswordResetOtp("john123");

        ArgumentCaptor<com.shippex.dto.otp.GenerateOtpRequest> captor =
                ArgumentCaptor.forClass(com.shippex.dto.otp.GenerateOtpRequest.class);
        verify(otpService).generateOtp(captor.capture());
        assertThat(captor.getValue().getPhoneNumber()).isEqualTo("+919876543210");
    }

    @Test
    void resetPassword_verifiesOtpHashesAndPersistsPassword() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secret")).thenReturn("hashed-secret");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.resetPassword("john123", "123456", "new-secret");

        assertThat(user.getPassword()).isEqualTo("hashed-secret");
        ArgumentCaptor<VerifyOtpRequest> captor = ArgumentCaptor.forClass(VerifyOtpRequest.class);
        verify(otpService).verifyOtp(captor.capture());
        assertThat(captor.getValue().getPhoneNumber()).isEqualTo("+919876543210");
        assertThat(captor.getValue().getOtp()).isEqualTo("123456");
        verify(appUserRepository).save(user);
    }

    @Test
    void resetPassword_doesNotSaveWhenOtpIsInvalid() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByUsername("john123")).thenReturn(Optional.of(user));
        doThrow(new OtpException("Invalid OTP.")).when(otpService).verifyOtp(any(VerifyOtpRequest.class));

        assertThatThrownBy(() -> appUserService.resetPassword("john123", "000000", "new-secret"))
                .isInstanceOf(OtpException.class)
                .hasMessage("Invalid OTP.");
        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void resetPassword_missingUserFailsWithoutVerifyingOtpOrSaving() {
        when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.resetPassword("missing", "123456", "new-secret"))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(otpService, never()).verifyOtp(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void generatePasswordResetOtpForPhone_usesRegisteredNumber() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByWhatsappContactNo("+919876543210")).thenReturn(Optional.of(user));

        appUserService.generatePasswordResetOtpForPhone("+919876543210");

        verify(otpService).generateOtp(argThat(request -> request.getPhoneNumber().equals("+919876543210")));
    }

    @Test
    void generatePasswordResetOtpForPhone_rejectsUnknownNumber() {
        when(appUserRepository.findByWhatsappContactNo("+12025550123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.generatePasswordResetOtpForPhone("+12025550123"))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(otpService, never()).generateOtp(any());
    }

    @Test
    void resetPasswordByPhone_verifiesOtpAndPersistsEncodedPassword() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByWhatsappContactNo("+919876543210")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secret")).thenReturn("hashed-secret");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.resetPasswordByPhone("+919876543210", "123456", "new-secret");

        assertThat(user.getPassword()).isEqualTo("hashed-secret");
        verify(otpService).verifyOtp(argThat(request -> request.getPhoneNumber().equals("+919876543210") && request.getOtp().equals("123456")));
        verify(appUserRepository).save(user);
    }

    @Test
    void resetPasswordByPhone_doesNotPersistForInvalidOtp() {
        AppUser user = new AppUser();
        user.setWhatsappContactNo("+919876543210");
        when(appUserRepository.findByWhatsappContactNo("+919876543210")).thenReturn(Optional.of(user));
        doThrow(new OtpException("Invalid OTP.")).when(otpService).verifyOtp(any(VerifyOtpRequest.class));

        assertThatThrownBy(() -> appUserService.resetPasswordByPhone("+919876543210", "000000", "new-secret"))
                .isInstanceOf(OtpException.class);
        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any());
    }
}
