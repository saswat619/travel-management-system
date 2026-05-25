package com.travel.analytics.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReportDto {
    private Long id;
    private String scope;
    private String metrics;
    private LocalDate generatedDate;
    private String generatedBy;
    private String period;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Long bookingVolume;
    private Long cancellationCount;
    private BigDecimal cancellationRate;
    private BigDecimal totalRevenue;
    private BigDecimal avgSpendPerTraveler;
    private Long totalTravelers;
    private Long activePartners;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
