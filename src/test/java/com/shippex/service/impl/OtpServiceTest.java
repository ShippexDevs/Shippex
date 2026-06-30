package com.shippex.service.impl;

import com.shippex.constants.RedisConstants;
import com.shippex.dto.notification.NotificationRequest;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.exception.OtpException;
import com.shippex.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private OtpService otpService;

    private GenerateOtpRequest generateRequest;
    private VerifyOtpRequest verifyRequest;

    @BeforeEach
    void setUp() {

        generateRequest = new GenerateOtpRequest();
        generateRequest.setPhoneNumber("9876543210");

        verifyRequest = new VerifyOtpRequest();
        verifyRequest.setPhoneNumber("9876543210");
        verifyRequest.setOtp("123456");
    }

    @Test
    void generateOtp_shouldGenerateOtpSuccessfully() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(1);

        otpService.generateOtp(generateRequest);

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(redisService).set(
                eq(RedisConstants.getOtpKey(normalizedPhone)),
                otpCaptor.capture(),
                eq(RedisConstants.OTP_TTL));

        String otp = otpCaptor.getValue();

        assertThat(otp)
                .hasSize(RedisConstants.OTP_LENGTH)
                .matches("\\d+");

        verify(redisService).delete(
                RedisConstants.getVerifiedKey(normalizedPhone));

        verify(redisService).incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL);

        ArgumentCaptor<NotificationRequest> notificationCaptor =
                ArgumentCaptor.forClass(NotificationRequest.class);

        verify(whatsAppService)
                .sendTextMessage(notificationCaptor.capture());

        NotificationRequest notification =
                notificationCaptor.getValue();

        assertThat(notification.getRecipient())
                .isEqualTo(normalizedPhone);

        assertThat(notification.getMessage())
                .contains("Your verification OTP is:")
                .contains("This OTP is valid");
    }

    @Test
    void generateOtp_shouldThrow_whenRetryLimitExceeded() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(RedisConstants.MAX_OTP_GENERATION_RETRIES);

        when(redisService.getRemainingTTL(
                RedisConstants.getGenerateRetryKey(normalizedPhone)))
                .thenReturn(120L);

        assertThatThrownBy(() ->
                otpService.generateOtp(generateRequest))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("Maximum OTP requests reached");

        verify(redisService, never())
                .set(anyString(), any(), any(Duration.class));

        verify(redisService, never())
                .delete(anyString());

        verify(redisService, never())
                .incrementRetryCount(anyString(), any(Duration.class));

        verifyNoInteractions(whatsAppService);
    }

    @Test
    void generateOtp_shouldWrapUnexpectedExceptions() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenThrow(new RuntimeException("Redis down"));

        assertThatThrownBy(() ->
                otpService.generateOtp(generateRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Unable to generate OTP.")
                .hasCauseInstanceOf(RuntimeException.class);

        verifyNoInteractions(whatsAppService);
    }

    @Test
    void generateOtp_shouldTreatNullRetryCountAsZero() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(1);

        otpService.generateOtp(generateRequest);

        verify(redisService).incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL);

        verify(whatsAppService).sendTextMessage(any(NotificationRequest.class));
    }

    @Test
    void generateOtp_shouldDeleteVerifiedFlagBeforeGeneratingNewOtp() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(2);

        when(redisService.incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(3);

        otpService.generateOtp(generateRequest);

        verify(redisService).delete(
                RedisConstants.getVerifiedKey(normalizedPhone));
    }

    @Test
    void generateOtp_shouldStoreSixDigitNumericOtp() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(1);

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        otpService.generateOtp(generateRequest);

        verify(redisService).set(
                eq(RedisConstants.getOtpKey(normalizedPhone)),
                otpCaptor.capture(),
                eq(RedisConstants.OTP_TTL));

        String otp = otpCaptor.getValue();

        assertThat(otp)
                .hasSize(6)
                .containsOnlyDigits();
    }

    @Test
    void generateOtp_shouldNormalizeIndianPhoneNumber() {

        generateRequest.setPhoneNumber("98765 43210");

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.incrementRetryCount(
                RedisConstants.getGenerateRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(1);

        otpService.generateOtp(generateRequest);

        verify(redisService).set(
                eq(RedisConstants.getOtpKey(normalizedPhone)),
                any(String.class),
                eq(RedisConstants.OTP_TTL));

        verify(whatsAppService)
                .sendTextMessage(any(NotificationRequest.class));
    }

    @Test
    void generateOtp_shouldThrow_whenPhoneNumberIsBlank() {

        generateRequest.setPhoneNumber(" ");

        assertThatThrownBy(() ->
                otpService.generateOtp(generateRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Phone number cannot be empty.");

        verifyNoInteractions(redisService);
        verifyNoInteractions(whatsAppService);
    }

    @Test
    void generateOtp_shouldThrow_whenPhoneNumberIsNull() {

        generateRequest.setPhoneNumber(null);

        assertThatThrownBy(() ->
                otpService.generateOtp(generateRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Phone number cannot be empty.");

        verifyNoInteractions(redisService);
        verifyNoInteractions(whatsAppService);
    }


    // ============================================================
    // verifyOtp() - Success Scenarios
    // ============================================================

    @Test
    void verifyOtp_shouldVerifySuccessfully() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("123456");

        otpService.verifyOtp(verifyRequest);

        verify(redisService).set(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.TRUE,
                RedisConstants.OTP_VERIFIED_TTL);

        verify(redisService).delete(
                RedisConstants.getVerifyRetryKey(normalizedPhone));

        verify(redisService, never())
                .incrementRetryCount(anyString(), any(Duration.class));
    }

    @Test
    void verifyOtp_shouldTreatNullRetryCountAsZero() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("123456");

        otpService.verifyOtp(verifyRequest);

        verify(redisService).set(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.TRUE,
                RedisConstants.OTP_VERIFIED_TTL);
    }

    @Test
    void verifyOtp_shouldDeleteRetryCounterAfterSuccessfulVerification() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(2);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("123456");

        otpService.verifyOtp(verifyRequest);

        verify(redisService).delete(
                RedisConstants.getVerifyRetryKey(normalizedPhone));
    }

    @Test
    void verifyOtp_shouldStoreVerifiedFlagForOneHour() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(1);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("123456");

        otpService.verifyOtp(verifyRequest);

        verify(redisService).set(
                eq(RedisConstants.getVerifiedKey(normalizedPhone)),
                eq(Boolean.TRUE),
                eq(RedisConstants.OTP_VERIFIED_TTL));
    }

    @Test
    void verifyOtp_shouldNormalizePhoneNumberBeforeVerification() {

        verifyRequest.setPhoneNumber("98765-43210");

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("123456");

        otpService.verifyOtp(verifyRequest);

        verify(redisService).get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class);
    }


    // ============================================================
    // verifyOtp() - Failure Scenarios
    // ============================================================

    @Test
    void verifyOtp_shouldThrow_whenRetryLimitExceeded() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(RedisConstants.MAX_OTP_VERIFICATION_RETRIES);

        when(redisService.getRemainingTTL(
                RedisConstants.getVerifyRetryKey(normalizedPhone)))
                .thenReturn(300L);

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("Too many invalid OTP attempts");

        verify(redisService, never())
                .get(RedisConstants.getOtpKey(normalizedPhone), String.class);

        verify(redisService, never())
                .set(anyString(), any(), any(Duration.class));

        verify(redisService, never())
                .delete(anyString());
    }

    @Test
    void verifyOtp_shouldThrow_whenOtpHasExpired() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn(null);

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("OTP has expired. Please generate a new OTP.");

        verify(redisService, never())
                .set(anyString(), any(), any(Duration.class));
    }

    @Test
    void verifyOtp_shouldThrow_whenOtpIsInvalid() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(null);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("654321");

        when(redisService.incrementRetryCount(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(1);

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Invalid OTP.");

        verify(redisService).incrementRetryCount(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL);

        verify(redisService, never())
                .set(anyString(), any(), any(Duration.class));
    }

    @Test
    void verifyOtp_shouldIncrementRetryCount_whenOtpIsInvalid() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenReturn(3);

        when(redisService.get(
                RedisConstants.getOtpKey(normalizedPhone),
                String.class))
                .thenReturn("111111");

        when(redisService.incrementRetryCount(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL))
                .thenReturn(4);

        verifyRequest.setOtp("222222");

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class);

        verify(redisService).incrementRetryCount(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                RedisConstants.OTP_TTL);
    }

    @Test
    void verifyOtp_shouldWrapUnexpectedExceptions() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifyRetryKey(normalizedPhone),
                Integer.class))
                .thenThrow(new RuntimeException("Redis unavailable"));

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Unable to verify OTP.")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(redisService, never())
                .set(anyString(), any(), any(Duration.class));
    }

    @Test
    void verifyOtp_shouldThrow_whenPhoneNumberIsBlank() {

        verifyRequest.setPhoneNumber(" ");

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Phone number cannot be empty.");

        verifyNoInteractions(redisService);
    }

    @Test
    void verifyOtp_shouldThrow_whenPhoneNumberIsNull() {

        verifyRequest.setPhoneNumber(null);

        assertThatThrownBy(() ->
                otpService.verifyOtp(verifyRequest))
                .isInstanceOf(OtpException.class)
                .hasMessage("Phone number cannot be empty.");

        verifyNoInteractions(redisService);
    }

    // ============================================================
    // isOtpVerified() Tests
    // ============================================================

    @Test
    void isOtpVerified_shouldReturnTrue_whenVerifiedFlagExists() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class))
                .thenReturn(Boolean.TRUE);

        boolean result = otpService.isOtpVerified("9876543210");

        assertThat(result).isTrue();

        verify(redisService).get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class);
    }

    @Test
    void isOtpVerified_shouldReturnFalse_whenVerifiedFlagIsFalse() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class))
                .thenReturn(Boolean.FALSE);

        boolean result = otpService.isOtpVerified("9876543210");

        assertThat(result).isFalse();
    }

    @Test
    void isOtpVerified_shouldReturnFalse_whenVerifiedFlagDoesNotExist() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class))
                .thenReturn(null);

        boolean result = otpService.isOtpVerified("9876543210");

        assertThat(result).isFalse();
    }

    @Test
    void isOtpVerified_shouldNormalizePhoneNumber() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class))
                .thenReturn(Boolean.TRUE);

        otpService.isOtpVerified("98765-43210");

        verify(redisService).get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class);
    }

    @Test
    void isOtpVerified_shouldReturnFalse_whenRedisThrowsException() {

        String normalizedPhone = "+919876543210";

        when(redisService.get(
                RedisConstants.getVerifiedKey(normalizedPhone),
                Boolean.class))
                .thenThrow(new RuntimeException("Redis down"));

        boolean result = otpService.isOtpVerified("9876543210");

        assertThat(result).isFalse();
    }

    @Test
    void isOtpVerified_shouldReturnFalse_whenPhoneNumberIsBlank() {

        assertThat(otpService.isOtpVerified(" "))
                .isFalse();

        verifyNoInteractions(redisService);
    }

    @Test
    void isOtpVerified_shouldReturnFalse_whenPhoneNumberIsNull() {

        assertThat(otpService.isOtpVerified(null))
                .isFalse();

        verifyNoInteractions(redisService);
    }
}