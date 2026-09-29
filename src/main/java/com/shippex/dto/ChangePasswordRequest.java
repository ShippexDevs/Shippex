package com.shippex.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password must not be blank.") String currentPassword,
        @NotBlank(message = "New password must not be blank.") String newPassword
) {
}
