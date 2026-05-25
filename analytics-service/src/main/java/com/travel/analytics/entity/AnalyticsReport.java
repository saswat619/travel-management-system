package com.travel.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "analytics_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reportName;

    private String reportType;

    private String generatedBy;

    private LocalDate reportPeriodStart;

    private LocalDate reportPeriodEnd;

    private Long totalRecords;

    @Column(columnDefinition = "TEXT")
    private String reportData;

    private String status;
}
