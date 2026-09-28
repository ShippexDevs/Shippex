package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.dashboard.DailyOrderOverviewResponse;
import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.RecentActivityResponse;
import com.shippex.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.shippex.util.Pagination;

@RestController
@Validated
@RequestMapping("/api/admin/dashboard")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/widgets")
    public ResponseEntity<ApiResponse<DashboardWidgetsResponse>> getWidgets() {
        log.debug("Admin dashboard widgets request received");
        return ResponseEntity.ok(ApiResponse.success("Dashboard widgets retrieved successfully.",
                dashboardService.getWidgets()));
    }

    @GetMapping("/overviewChart")
    public ResponseEntity<ApiResponse<List<DailyOrderOverviewResponse>>> getOverviewChart(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        log.debug("Admin dashboard overview chart request received for days={}", days);
        return ResponseEntity.ok(ApiResponse.success("Order overview retrieved successfully.",
                dashboardService.getOverviewChart(days)));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<AdminOrderResponse>>> getRecentOrders(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        log.debug("Admin dashboard recent orders request received for days={}, offset={}, limit={}", days, offset, limit);
        return ResponseEntity.ok(ApiResponse.success("Recent orders retrieved successfully.",
                Pagination.slice(dashboardService.getRecentOrders(days), offset, limit)));
    }

    public ResponseEntity<ApiResponse<List<AdminOrderResponse>>> getRecentOrders(int days) {
        return getRecentOrders(days, 0, 10);
    }

    @GetMapping("/recentActivity")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        log.debug("Admin dashboard recent activity request received for days={}", days);
        return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved successfully.",
                dashboardService.getRecentActivity(days)));
    }
}
