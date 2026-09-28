package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.order.OrderResponse;
import com.shippex.dto.order.PlaceOrderRequest;
import com.shippex.mapper.OrderMapper;
import com.shippex.model.Order;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.shippex.util.Pagination;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PlaceOrderRequest request) {
        log.info("Order placement request received for userId={} with itemCount={}", user.getId(), request.getItems().size());
        Order order = orderService.placeOrder(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully.", OrderMapper.toResponse(order)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        log.debug("Order history request received for userId={}", user.getId());
        List<OrderResponse> orders = orderService.getOrdersForUser(user.getId()).stream()
                .map(OrderMapper::toResponse).toList();
        orders = Pagination.slice(orders, offset, limit);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.", orders));
    }

    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(CustomUserDetails user) {
        return getMyOrders(user, 0, 10);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails user) {
        log.info("Order cancellation request received for orderId={}, userId={}", id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully.",
                OrderMapper.toResponse(orderService.cancelOrder(id, user.getId()))));
    }
}
