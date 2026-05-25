package com.travel.compliance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Scope of the report: GDPR / REGULATORY / SECURITY / DATA_RETENTION / FINANCIAL
    @NotBlank
    @Column(nullable = false, length = 100)
    private String scope;

    // JSON or structured string of metrics e.g. {"totalAuditLogs":500,"violations":3}
    @Column(columnDefinition = "TEXT")
    private String metrics;

    @Column(nullable = false)
    private LocalDate generatedDate;

    private String generatedBy;

    // GDPR_COMPLIANCE / SECURITY_AUDIT / DATA_RETENTION / REGULATORY_SUBMISSION
    @NotBlank
    private String reportType;

    // DRAFT / SUBMITTED / APPROVED / ARCHIVED
    @Builder.Default
    private String status = "DRAFT";

    @Column(length = 500)
    private String description;

    // Period covered by the report
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Link to department or region this report covers
    private String department;

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
