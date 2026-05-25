package com.travel.analytics.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private Long totalBookings;
    private BigDecimal totalRevenue;
    private Long totalPartners;
    private Long totalPackages;
    private Long activeUsers;
    private List<DashboardMetricDto> metrics;
    private String generatedAt;
}
