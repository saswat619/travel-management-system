package com.travel.compliance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecordDto {
    private Long id;
    private String recordType;
    private String title;
    private String description;
    private String affectedResource;
    private String affectedUserId;
    private String severity;
    private String status;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String notes;
    private LocalDateTime createdAt;
}
