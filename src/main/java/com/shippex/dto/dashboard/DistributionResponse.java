package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DistributionResponse {
    private String label;
    private long count;
    private double percentage;
}
