package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.order.OrderResponse;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.mapper.OrderMapper;
import com.shippex.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/admin/orders")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        log.debug("Admin request received to fetch all orders");
        List<OrderResponse> orders = Pagination.slice(orderService.getAllOrdersForAdmin(), offset, limit);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.", orders));
    }

    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return getAllOrders(0, 10);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info(
                "Admin order-status update request received for orderId={}, targetStatus={}",
                id,
                request.getStatus()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order status updated successfully.",
                        OrderMapper.toResponse(
                                orderService.updateStatus(id, request)
                        )
                )
        );
    }
}
