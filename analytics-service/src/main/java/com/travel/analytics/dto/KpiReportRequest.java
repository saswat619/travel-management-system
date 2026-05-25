package com.travel.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class KpiReportRequest {

    @NotBlank(message = "Scope is required (BOOKING/REVENUE/CANCELLATION/SPEND_PER_TRAVELER/PARTNER_PERFORMANCE/OVERALL)")
    private String scope;

    @NotBlank(message = "Period is required (DAILY/WEEKLY/MONTHLY/QUARTERLY/ANNUAL)")
    private String period;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    // Optional manual input; if null, auto-calculated from feign clients
    private Long bookingVolume;
    private Long cancellationCount;
    private BigDecimal totalRevenue;
    private BigDecimal avgSpendPerTraveler;
    private Long totalTravelers;
    private Long activePartners;
    private String notes;
}
