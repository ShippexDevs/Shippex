package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.order.OrderResponse;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.mapper.OrderMapper;
import com.shippex.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders().stream().map(OrderMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.", orders));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully.",
                OrderMapper.toResponse(orderService.updateStatus(id, request))));
    }
}
