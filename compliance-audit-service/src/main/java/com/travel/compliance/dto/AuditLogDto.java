package com.travel.compliance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private String userId;
    private String action;
    private String resource;
    private String resourceId;
    private String description;
    private String ipAddress;
    private String requestMethod;
    private String requestUri;
    private Integer responseStatus;
    private Long executionTimeMs;
    private boolean success;
    private String errorMessage;
    private LocalDateTime timestamp;
}
