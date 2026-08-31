package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardMetricResponse {
    private BigDecimal total;
    private BigDecimal changeSinceLastMonth;
    private BigDecimal currentMonth;
    private BigDecimal previousMonth;
}
