package com.travel.compliance.controller;

import com.travel.compliance.dto.AuditLogDto;
import com.travel.compliance.dto.AuditLogRequest;
import com.travel.compliance.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/compliance/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<Page<AuditLogDto>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAllAuditLogs(pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAuditLogsByUser(userId, pageable));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAuditLogsByAction(action, pageable));
    }

    @GetMapping("/resource/{resource}")
    @Operation(summary = "Get audit logs by resource")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsByResource(
            @PathVariable String resource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAuditLogsByResource(resource, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAuditLogsByDateRange(start, end, pageable));
    }

    @GetMapping("/failed")
    @Operation(summary = "Get failed actions")
    public ResponseEntity<Page<AuditLogDto>> getFailedActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getFailedActions(pageable));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get action statistics")
    public ResponseEntity<Map<String, Long>> getActionStatistics() {
        return ResponseEntity.ok(auditLogService.getActionStatistics());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER','PARTNER')")
    @Operation(summary = "Create audit log")
    public ResponseEntity<AuditLogDto> createAuditLog(@RequestBody AuditLogRequest request) {
        return ResponseEntity.ok(auditLogService.createAuditLog(request));
    }
}
