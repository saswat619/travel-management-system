package com.travel.compliance.service;

import com.travel.compliance.annotation.Auditable;
import com.travel.compliance.dto.ComplianceReportDto;
import com.travel.compliance.dto.ComplianceReportRequest;
import com.travel.compliance.entity.ComplianceReport;
import com.travel.compliance.exception.ResourceNotFoundException;
import com.travel.compliance.repository.AuditLogRepository;
import com.travel.compliance.repository.ComplianceReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceReportService {

    private final ComplianceReportRepository complianceReportRepository;
    private final AuditLogRepository auditLogRepository;

    public Page<ComplianceReportDto> getAllReports(Pageable pageable) {
        return complianceReportRepository.findAll(pageable).map(this::mapToDto);
    }

    public ComplianceReportDto getReportById(Long id) {
        return complianceReportRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceReport", "id", id));
    }

    public Page<ComplianceReportDto> getReportsByScope(String scope, Pageable pageable) {
        return complianceReportRepository.findByScope(scope, pageable).map(this::mapToDto);
    }

    public Page<ComplianceReportDto> getReportsByType(String reportType, Pageable pageable) {
        return complianceReportRepository.findByReportType(reportType, pageable).map(this::mapToDto);
    }

    public Page<ComplianceReportDto> getReportsByStatus(String status, Pageable pageable) {
        return complianceReportRepository.findByStatus(status, pageable).map(this::mapToDto);
    }

    public List<ComplianceReportDto> getReportsByPeriod(LocalDate start, LocalDate end) {
        return complianceReportRepository.findByPeriod(start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    @Auditable(action = "CREATE", resource = "ComplianceReport", description = "Generating compliance report")
    public ComplianceReportDto generateReport(ComplianceReportRequest req) {
        log.info("Generating compliance report of type: {} for scope: {}", req.getReportType(), req.getScope());

        String currentUser = getCurrentUsername();

        // Auto-generate metrics from audit logs if not provided
        String metrics = req.getMetrics();
        if (metrics == null || metrics.isBlank()) {
            long totalLogs = auditLogRepository.count();
            metrics = String.format(
                "{\"totalAuditLogs\":%d,\"reportScope\":\"%s\",\"period\":\"%s to %s\"}",
                totalLogs, req.getScope(), req.getPeriodStart(), req.getPeriodEnd()
            );
        }

        ComplianceReport report = ComplianceReport.builder()
                .scope(req.getScope())
                .metrics(metrics)
                .generatedDate(LocalDate.now())
                .generatedBy(currentUser)
                .reportType(req.getReportType())
                .status("DRAFT")
                .description(req.getDescription())
                .periodStart(req.getPeriodStart())
                .periodEnd(req.getPeriodEnd())
                .department(req.getDepartment())
                .build();

        return mapToDto(complianceReportRepository.save(report));
    }

    @Transactional
    public ComplianceReportDto updateReportStatus(Long id, String status) {
        ComplianceReport report = complianceReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceReport", "id", id));
        report.setStatus(status);
        return mapToDto(complianceReportRepository.save(report));
    }

    public Map<String, Long> getScopeStatistics() {
        List<String> scopes = List.of("GDPR", "REGULATORY", "SECURITY", "DATA_RETENTION", "FINANCIAL");
        return scopes.stream()
                .collect(Collectors.toMap(
                        scope -> scope,
                        scope -> complianceReportRepository.countActiveByScope(scope)
                ));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private ComplianceReportDto mapToDto(ComplianceReport r) {
        return ComplianceReportDto.builder()
                .id(r.getId())
                .scope(r.getScope())
                .metrics(r.getMetrics())
                .generatedDate(r.getGeneratedDate())
                .generatedBy(r.getGeneratedBy())
                .reportType(r.getReportType())
                .status(r.getStatus())
                .description(r.getDescription())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .department(r.getDepartment())
                .createdAt(r.getCreatedAt())
                .createdBy(r.getCreatedBy())
                .build();
    }
}
