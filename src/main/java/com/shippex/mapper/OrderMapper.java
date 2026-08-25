package com.shippex.mapper;

import com.shippex.dto.order.OrderItemResponse;
import com.shippex.dto.order.OrderResponse;
import com.shippex.dto.order.DeliveryDestinationResponse;
import com.shippex.model.DeliveryDestination;
import com.shippex.model.Order;
import com.shippex.model.OrderItem;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .items(order.getItems() == null ? null : order.getItems().stream()
                        .map(OrderMapper::toItemResponse)
                        .toList())
                .currency(order.getCurrency())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryDestination(toDeliveryDestinationResponse(order.getDeliveryDestination()))
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .deliveryInstructions(order.getDeliveryInstructions())
                .orderInstructions(order.getOrderInstructions())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .sku(item.getSku())
                .name(item.getName())
                .image(item.getImage())
                .unit(item.getUnit())
                .currency(item.getCurrency())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    private static DeliveryDestinationResponse toDeliveryDestinationResponse(
            DeliveryDestination destination) {
        if (destination == null) {
            return null;
        }
        return DeliveryDestinationResponse.builder()
                .shipName(destination.getShipName())
                .imoNumber(destination.getImoNumber())
                .berthNumber(destination.getBerthNumber())
                .portName(destination.getPortName())
                .build();
    }
}
