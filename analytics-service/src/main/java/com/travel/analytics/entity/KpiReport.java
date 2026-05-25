package com.travel.analytics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class KpiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Scope: BOOKING / REVENUE / CANCELLATION / SPEND_PER_TRAVELER / PARTNER_PERFORMANCE / OVERALL
    @NotBlank
    @Column(nullable = false, length = 100)
    private String scope;

    // JSON string of KPI metrics
    // e.g. {"bookingVolume":1500,"cancellationRate":0.05,"avgSpendPerTraveler":45000.00}
    @Column(columnDefinition = "TEXT")
    private String metrics;

    @Column(nullable = false)
    private LocalDate generatedDate;

    private String generatedBy;

    // DAILY / WEEKLY / MONTHLY / QUARTERLY / ANNUAL
    @NotBlank
    private String period;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Key numeric KPIs stored separately for easy querying
    private Long bookingVolume;
    private Long cancellationCount;
    private BigDecimal cancellationRate;
    private BigDecimal totalRevenue;
    private BigDecimal avgSpendPerTraveler;
    private Long totalTravelers;
    private Long activePartners;

    // DRAFT / PUBLISHED / ARCHIVED
    @Builder.Default
    private String status = "DRAFT";

    @Column(length = 500)
    private String notes;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
