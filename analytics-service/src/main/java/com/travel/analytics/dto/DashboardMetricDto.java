package com.travel.analytics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricDto {
    private Long id;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private String category;
    private LocalDateTime lastUpdated;
}
