package com.shippex.dto.product;

import jakarta.validation.constraints.NotNull;

public record UpdateActiveRequest(@NotNull Boolean active) {}
