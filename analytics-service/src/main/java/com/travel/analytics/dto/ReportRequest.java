package com.travel.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {

    @NotBlank
    private String reportName;

    @NotBlank
    private String reportType;

    @NotNull
    private LocalDate reportPeriodStart;

    @NotNull
    private LocalDate reportPeriodEnd;
}
