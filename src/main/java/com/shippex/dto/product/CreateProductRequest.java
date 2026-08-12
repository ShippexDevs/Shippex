package com.shippex.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateProductRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String brand;

    @NotBlank
    private String sku;

    @NotBlank
    private String description;

    @NotBlank
    private String category;

    @NotBlank
    private String categorySlug;

    @NotEmpty
    private List<String> images;

    @NotBlank
    private String currency;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal currentPrice;

    @DecimalMin("0.0")
    private BigDecimal originalPrice;

    @NotBlank
    private String unit;

    @NotNull
    private Integer stock;

    private Boolean featured;

    private Boolean active;

    private Integer displayOrder;

    private String deliveryTime;

    private List<String> tags;

}
