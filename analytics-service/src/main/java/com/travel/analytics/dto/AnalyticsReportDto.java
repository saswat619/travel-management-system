package com.travel.analytics.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReportDto {
    private Long id;
    private String reportName;
    private String reportType;
    private String generatedBy;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;
    private Long totalRecords;
    private String reportData;
    private String status;
    private LocalDateTime createdAt;
}
