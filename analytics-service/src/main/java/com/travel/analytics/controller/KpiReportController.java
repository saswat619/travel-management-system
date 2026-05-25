package com.travel.analytics.controller;

import com.travel.analytics.dto.KpiReportDto;
import com.travel.analytics.dto.KpiReportRequest;
import com.travel.analytics.service.KpiReportService;
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
@RequestMapping("/api/analytics/kpi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KPI Reports", description = "KPI tracking: Booking Volume, Cancellation Rate, Spend per Traveler, Revenue")
@SecurityRequirement(name = "bearerAuth")
public class KpiReportController {

    private final KpiReportService kpiReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get all KPI reports with pagination")
    public ResponseEntity<Page<KpiReportDto>> getAllKpiReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kpiReportService.getAllKpiReports(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get KPI report by ID")
    public ResponseEntity<KpiReportDto> getKpiReportById(@PathVariable Long id) {
        return ResponseEntity.ok(kpiReportService.getKpiReportById(id));
    }

    @GetMapping("/scope/{scope}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get KPI reports by scope (BOOKING/REVENUE/CANCELLATION/SPEND_PER_TRAVELER/OVERALL)")
    public ResponseEntity<Page<KpiReportDto>> getKpiReportsByScope(
            @PathVariable String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kpiReportService.getKpiReportsByScope(scope, PageRequest.of(page, size)));
    }

    @GetMapping("/period/{period}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get KPI reports by period (DAILY/WEEKLY/MONTHLY/QUARTERLY/ANNUAL)")
    public ResponseEntity<Page<KpiReportDto>> getKpiReportsByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(kpiReportService.getKpiReportsByPeriod(period, PageRequest.of(page, size)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get KPI reports by date range")
    public ResponseEntity<List<KpiReportDto>> getKpiReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(kpiReportService.getKpiReportsByDateRange(start, end));
    }

    @GetMapping("/scope/{scope}/latest")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get latest KPI reports for a given scope")
    public ResponseEntity<List<KpiReportDto>> getLatestKpiByScope(@PathVariable String scope) {
        return ResponseEntity.ok(kpiReportService.getLatestKpiByScope(scope));
    }

    @GetMapping("/cancellation-rate")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER','CORPORATE_MANAGER')")
    @Operation(summary = "Get average cancellation rate by period")
    public ResponseEntity<Map<String, Object>> getAvgCancellationRate(@RequestParam String period) {
        Double rate = kpiReportService.getAvgCancellationRate(period);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("period", period);
        result.put("avgCancellationRate", rate != null ? rate : 0.0);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER')")
    @Operation(summary = "Generate a new KPI report")
    public ResponseEntity<KpiReportDto> generateKpiReport(@Valid @RequestBody KpiReportRequest req) {
        log.info("Generating KPI report: scope={}, period={}", req.getScope(), req.getPeriod());
        return ResponseEntity.status(HttpStatus.CREATED).body(kpiReportService.generateKpiReport(req));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Publish a KPI report")
    public ResponseEntity<KpiReportDto> publishKpiReport(@PathVariable Long id) {
        return ResponseEntity.ok(kpiReportService.publishKpiReport(id));
    }
}
