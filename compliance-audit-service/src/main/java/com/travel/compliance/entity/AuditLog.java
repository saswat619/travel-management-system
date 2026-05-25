package com.travel.compliance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(length = 100)
    private String action;

    @Column(length = 100)
    private String resource;

    private String resourceId;

    @Column(length = 500)
    private String description;

    private String ipAddress;

    private String userAgent;

    private String requestMethod;

    private String requestUri;

    private Integer responseStatus;

    private Long executionTimeMs;

    @Builder.Default
    private boolean success = true;

    private String errorMessage;

    private LocalDateTime timestamp;
}
