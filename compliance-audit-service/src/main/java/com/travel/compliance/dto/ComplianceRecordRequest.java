package com.travel.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComplianceRecordRequest {

    @NotBlank
    private String recordType;

    @NotBlank
    private String title;

    private String description;

    private String affectedResource;

    private String affectedUserId;

    @NotBlank
    private String severity;

    private String notes;
}
