package com.shippex.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "Invalid phone number format."
    )
    private String phoneNumber;

    @NotBlank(message = "OTP is required.")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be exactly 6 digits."
    )
    private String otp;

}