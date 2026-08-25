package com.shippex.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class PlaceOrderRequest {
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
    @NotNull
    @Valid
    private DeliveryDestinationRequest deliveryDestination;
    private LocalDate estimatedDeliveryDate;
    private LocalTime estimatedDeliveryTime;
    private String deliveryInstructions;
    private String orderInstructions;
    @NotBlank
    private String paymentMethod;
}
