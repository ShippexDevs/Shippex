package com.shippex.dto.category;

import java.time.LocalDateTime;

public record CategoryResponse(String id, String name, String slug, String skuPrefix,
        String description, String imageUrl, Boolean active,
        LocalDateTime createdAt, LocalDateTime updatedAt) {}
