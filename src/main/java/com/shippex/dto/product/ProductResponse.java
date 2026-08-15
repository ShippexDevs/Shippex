package com.shippex.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProductResponse {

    private String id;

    private String name;

    private String brand;

    private String sku;

    private String description;

    private String category;

    private String categorySlug;

    private List<String> images;

    private String currency;

    private BigDecimal currentPrice;

    private BigDecimal originalPrice;

    private String unit;

    private Double ratings;

    private Integer stock;

    private Boolean featured;

    private Boolean active;

    private Integer displayOrder;

    private String deliveryTime;

    private List<String> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}