package com.shippex.service;

import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.dashboard.DailyOrderOverviewResponse;
import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.RecentActivityResponse;

import java.util.List;

public interface DashboardService {
    DashboardWidgetsResponse getWidgets();
    List<DailyOrderOverviewResponse> getOverviewChart(int days);
    List<AdminOrderResponse> getRecentOrders(int days);
    List<RecentActivityResponse> getRecentActivity(int days);
}
