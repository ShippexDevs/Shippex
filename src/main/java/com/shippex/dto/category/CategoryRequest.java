package com.shippex.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9]{1,8}", message = "SKU prefix must be 1-8 letters or digits") String skuPrefix,
        String description,
        String imageUrl,
        Boolean active) {}
