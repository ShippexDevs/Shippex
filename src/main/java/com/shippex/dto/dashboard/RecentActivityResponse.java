package com.shippex.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentActivityResponse {
    private String type;
    private String description;
    private LocalDateTime occurredAt;
}
