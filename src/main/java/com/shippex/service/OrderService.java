package com.shippex.service;

import com.shippex.dto.order.PlaceOrderRequest;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.model.Order;

import java.util.List;

public interface OrderService {
    Order placeOrder(String userId, PlaceOrderRequest request);
    List<Order> getOrdersForUser(String userId);
    Order cancelOrder(String orderId, String userId);
    List<Order> getAllOrders();
    Order updateStatus(String orderId, UpdateOrderStatusRequest request);
}
