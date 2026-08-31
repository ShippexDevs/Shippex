package com.shippex.dto.dashboard;

import com.shippex.dto.order.OrderResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOrderResponse {
    private OrderResponse order;
    private OrderUserResponse user;
}
