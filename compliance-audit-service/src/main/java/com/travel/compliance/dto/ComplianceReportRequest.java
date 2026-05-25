package com.travel.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ComplianceReportRequest {

    @NotBlank(message = "Scope is required (GDPR/REGULATORY/SECURITY/DATA_RETENTION/FINANCIAL)")
    private String scope;

    @NotBlank(message = "Report type is required")
    private String reportType;

    private String metrics;

    private String description;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    private String department;
}
