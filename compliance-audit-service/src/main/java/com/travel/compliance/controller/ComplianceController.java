package com.travel.compliance.controller;

import com.travel.compliance.dto.ComplianceRecordDto;
import com.travel.compliance.dto.ComplianceRecordRequest;
import com.travel.compliance.service.ComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compliance/records")
@RequiredArgsConstructor
@Tag(name = "Compliance Records", description = "Compliance record management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ComplianceController {

    private final ComplianceService complianceService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all compliance records")
    public ResponseEntity<Page<ComplianceRecordDto>> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(complianceService.getAllRecords(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get compliance record by ID")
    public ResponseEntity<ComplianceRecordDto> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(complianceService.getRecordById(id));
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Get records by severity")
    public ResponseEntity<Page<ComplianceRecordDto>> getRecordsBySeverity(
            @PathVariable String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(complianceService.getRecordsBySeverity(severity, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get records by status")
    public ResponseEntity<Page<ComplianceRecordDto>> getRecordsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(complianceService.getRecordsByStatus(status, pageable));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open compliance records")
    public ResponseEntity<List<ComplianceRecordDto>> getOpenRecords() {
        return ResponseEntity.ok(complianceService.getOpenRecords());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get severity statistics")
    public ResponseEntity<Map<String, Long>> getSeverityStatistics() {
        return ResponseEntity.ok(complianceService.getSeverityStatistics());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create compliance record")
    public ResponseEntity<ComplianceRecordDto> createRecord(@Valid @RequestBody ComplianceRecordRequest request) {
        return ResponseEntity.ok(complianceService.createRecord(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update compliance record status")
    public ResponseEntity<ComplianceRecordDto> updateRecordStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String resolvedBy) {
        return ResponseEntity.ok(complianceService.updateRecordStatus(id, status, resolvedBy));
    }
}
