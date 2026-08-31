package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.dashboard.DailyOrderOverviewResponse;
import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.RecentActivityResponse;
import com.shippex.service.DashboardService;
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

@RestController
@Validated
@RequestMapping("/api/admin/dashboard")
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
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        log.debug("Admin dashboard recent orders request received for days={}", days);
        return ResponseEntity.ok(ApiResponse.success("Recent orders retrieved successfully.",
                dashboardService.getRecentOrders(days)));
    }

    @GetMapping("/recentActivity")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        log.debug("Admin dashboard recent activity request received for days={}", days);
        return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved successfully.",
                dashboardService.getRecentActivity(days)));
    }
}
