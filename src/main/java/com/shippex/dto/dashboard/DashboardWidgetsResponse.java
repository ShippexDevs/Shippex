package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardWidgetsResponse {
    private DashboardMetricResponse orders;
    private DashboardMetricResponse users;
    private DashboardMetricResponse products;
    private DashboardMetricResponse revenue;
    private List<DistributionResponse> productCategoryDistribution;
    private List<DistributionResponse> orderStatusDistribution;
}
