package com.travel.analytics.controller;

import com.travel.analytics.dto.AnalyticsReportDto;
import com.travel.analytics.dto.DashboardMetricDto;
import com.travel.analytics.dto.DashboardSummaryDto;
import com.travel.analytics.dto.ReportRequest;
import com.travel.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reports")
    public ResponseEntity<Page<AnalyticsReportDto>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(analyticsService.getAllReports(pageable));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "Get report by ID")
    public ResponseEntity<AnalyticsReportDto> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getReportById(id));
    }

    @PostMapping("/reports/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate analytics report")
    public ResponseEntity<AnalyticsReportDto> generateReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(analyticsService.generateReport(request));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/metrics/category/{category}")
    @Operation(summary = "Get metrics by category")
    public ResponseEntity<List<DashboardMetricDto>> getMetricsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(analyticsService.getMetricsByCategory(category));
    }

    @PostMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upsert dashboard metric")
    public ResponseEntity<DashboardMetricDto> upsertMetric(@RequestBody DashboardMetricDto dto) {
        return ResponseEntity.ok(analyticsService.upsertMetric(dto));
    }
}
