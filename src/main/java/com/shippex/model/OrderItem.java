package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
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
