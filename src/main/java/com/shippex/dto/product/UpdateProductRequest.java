package com.shippex.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String brand;

    private String description;

    @NotBlank
    private String category;

    @NotBlank
    private String categorySlug;

    private List<String> images;

    @NotBlank
    private String currency;

    @DecimalMin("0.0")
    private BigDecimal currentPrice;

    @DecimalMin("0.0")
    private BigDecimal originalPrice;

    private String unit;

    @Min(0)
    private Integer stock;

    private List<String> tags;

    private Boolean featured;

    private Boolean active;

    private String deliveryTime;

    private Integer displayOrder;
}