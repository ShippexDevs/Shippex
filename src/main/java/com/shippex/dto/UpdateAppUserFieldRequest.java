package com.shippex.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppUserFieldRequest(
        @NotBlank(message = "Value must not be blank.") String value
) {
}
