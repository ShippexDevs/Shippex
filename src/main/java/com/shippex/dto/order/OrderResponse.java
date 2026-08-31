package com.shippex.dto.order;

import com.shippex.model.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String userId;
    private List<OrderItemResponse> items;
    private String currency;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private DeliveryDestinationResponse deliveryDestination;
    private LocalDateTime estimatedDeliveryDateTime;
    private String deliveryInstructions;
    private String orderInstructions;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
