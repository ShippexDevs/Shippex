package com.shippex.dto.product;

import jakarta.validation.constraints.NotNull;

public record UpdateFeaturedRequest(@NotNull Boolean featured) {}
