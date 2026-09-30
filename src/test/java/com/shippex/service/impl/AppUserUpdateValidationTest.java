package com.shippex.service.impl;

import com.shippex.dto.ChangePasswordRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserUpdateValidationTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void passwordChange_shouldAcceptNonBlankCurrentAndNewPasswords() {
        assertThat(validator.validate(new ChangePasswordRequest("old-password", "new-password"))).isEmpty();
    }

    @Test
    void passwordChange_shouldRejectBlankCurrentOrNewPassword() {
        assertThat(validator.validate(new ChangePasswordRequest(" ", "new-password")))
                .extracting(ConstraintViolation::getMessage)
                .contains("Current password must not be blank.");
        assertThat(validator.validate(new ChangePasswordRequest("old-password", " ")))
                .extracting(ConstraintViolation::getMessage)
                .contains("New password must not be blank.");
        assertThat(validator.validate(new ChangePasswordRequest("", ""))).hasSize(2);
    }

    @Test
    void whatsappUpdate_shouldAcceptValidPhoneAndSixDigitOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhoneNumber("+12025550123");
        request.setOtp("123456");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void whatsappUpdate_shouldRejectBlankOrMalformedPhoneAndOtp() {
        VerifyOtpRequest blank = new VerifyOtpRequest();
        blank.setPhoneNumber(" ");
        blank.setOtp(" ");
        assertThat(validator.validate(blank)).hasSize(4);

        VerifyOtpRequest malformed = new VerifyOtpRequest();
        malformed.setPhoneNumber("not-a-phone");
        malformed.setOtp("12345");
        assertThat(validator.validate(malformed))
                .extracting("message")
                .contains("Invalid phone number format.", "OTP must be exactly 6 digits.");
    }
}
