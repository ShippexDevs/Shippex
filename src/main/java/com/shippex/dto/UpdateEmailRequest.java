package com.shippex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequest(
        @NotBlank(message = "Email must not be blank.")
        @Email(message = "Email must be valid.") String email
) {
}
