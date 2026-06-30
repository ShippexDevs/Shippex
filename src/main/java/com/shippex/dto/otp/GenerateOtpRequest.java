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
public class GenerateOtpRequest {

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "Invalid phone number format."
    )
    private String phoneNumber;

}