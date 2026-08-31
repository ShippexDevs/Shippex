package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    @Indexed(unique = true)
    private String orderNumber;
    @Indexed
    private String userId;
    private List<OrderItem> items;
    private String currency;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private DeliveryDestination deliveryDestination;
    private LocalDateTime estimatedDeliveryDateTime;
    private String deliveryInstructions;
    private String orderInstructions;
    private String paymentMethod;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
