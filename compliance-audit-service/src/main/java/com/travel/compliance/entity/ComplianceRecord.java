package com.travel.compliance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String recordType;

    @NotBlank
    private String title;

    private String description;

    private String affectedResource;

    private String affectedUserId;

    private String severity;

    private String status;

    private String resolvedBy;

    private LocalDateTime resolvedAt;

    private String notes;
}
