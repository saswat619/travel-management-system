package com.travel.compliance.dto;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String userId;
    private String action;
    private String resource;
    private String resourceId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String requestMethod;
    private String requestUri;
    private Integer responseStatus;
    private Long executionTimeMs;
    private boolean success;
    private String errorMessage;
}
