package com.shippex.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {
    private String productId;
    private String sku;
    private String name;
    private String image;
    private String unit;
    private String currency;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
