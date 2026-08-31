package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyOrderOverviewResponse {
    private LocalDate date;
    private long orderCount;
}
