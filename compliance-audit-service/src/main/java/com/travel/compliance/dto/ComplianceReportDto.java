package com.travel.compliance.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReportDto {
    private Long id;
    private String scope;
    private String metrics;
    private LocalDate generatedDate;
    private String generatedBy;
    private String reportType;
    private String status;
    private String description;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String department;
    private LocalDateTime createdAt;
    private String createdBy;
}
