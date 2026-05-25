package com.travel.compliance.controller;

import com.travel.compliance.dto.ComplianceReportDto;
import com.travel.compliance.dto.ComplianceReportRequest;
import com.travel.compliance.service.ComplianceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compliance/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Compliance Reports", description = "Generate and manage regulatory compliance reports (GDPR, Security, Data Retention)")
@SecurityRequirement(name = "bearerAuth")
public class ComplianceReportController {

    private final ComplianceReportService complianceReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get all compliance reports with pagination")
    public ResponseEntity<Page<ComplianceReportDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complianceReportService.getAllReports(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get compliance report by ID")
    public ResponseEntity<ComplianceReportDto> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(complianceReportService.getReportById(id));
    }

    @GetMapping("/scope/{scope}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get reports by scope (GDPR/REGULATORY/SECURITY/DATA_RETENTION/FINANCIAL)")
    public ResponseEntity<Page<ComplianceReportDto>> getReportsByScope(
            @PathVariable String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complianceReportService.getReportsByScope(scope, PageRequest.of(page, size)));
    }

    @GetMapping("/type/{reportType}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get reports by type")
    public ResponseEntity<Page<ComplianceReportDto>> getReportsByType(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complianceReportService.getReportsByType(reportType, PageRequest.of(page, size)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get reports by status (DRAFT/SUBMITTED/APPROVED/ARCHIVED)")
    public ResponseEntity<Page<ComplianceReportDto>> getReportsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(complianceReportService.getReportsByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get reports by period range")
    public ResponseEntity<List<ComplianceReportDto>> getReportsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(complianceReportService.getReportsByPeriod(start, end));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Generate a new compliance report")
    public ResponseEntity<ComplianceReportDto> generateReport(@Valid @RequestBody ComplianceReportRequest req) {
        log.info("Generating compliance report: type={}, scope={}", req.getReportType(), req.getScope());
        return ResponseEntity.status(HttpStatus.CREATED).body(complianceReportService.generateReport(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update compliance report status")
    public ResponseEntity<ComplianceReportDto> updateReportStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(complianceReportService.updateReportStatus(id, status));
    }

    @GetMapping("/statistics/scope")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Get report count statistics by scope")
    public ResponseEntity<Map<String, Long>> getScopeStatistics() {
        return ResponseEntity.ok(complianceReportService.getScopeStatistics());
    }
}
