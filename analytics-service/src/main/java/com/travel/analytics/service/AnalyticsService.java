package com.travel.analytics.service;

import com.travel.analytics.client.BookingAnalyticsClient;
import com.travel.analytics.client.PartnerAnalyticsClient;
import com.travel.analytics.dto.AnalyticsReportDto;
import com.travel.analytics.dto.DashboardMetricDto;
import com.travel.analytics.dto.DashboardSummaryDto;
import com.travel.analytics.dto.ReportRequest;
import com.travel.analytics.entity.AnalyticsReport;
import com.travel.analytics.entity.DashboardMetric;
import com.travel.analytics.exception.ResourceNotFoundException;
import com.travel.analytics.repository.AnalyticsReportRepository;
import com.travel.analytics.repository.DashboardMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsReportRepository analyticsReportRepository;
    private final DashboardMetricRepository dashboardMetricRepository;
    private final BookingAnalyticsClient bookingAnalyticsClient;
    private final PartnerAnalyticsClient partnerAnalyticsClient;

    public Page<AnalyticsReportDto> getAllReports(Pageable pageable) {
        return analyticsReportRepository.findAll(pageable).map(this::mapToDto);
    }

    public AnalyticsReportDto getReportById(Long id) {
        AnalyticsReport report = analyticsReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return mapToDto(report);
    }

    public AnalyticsReportDto generateReport(ReportRequest req) {
        String currentUser = getCurrentUsername();

        AnalyticsReport report = AnalyticsReport.builder()
                .reportName(req.getReportName())
                .reportType(req.getReportType())
                .generatedBy(currentUser)
                .reportPeriodStart(req.getReportPeriodStart())
                .reportPeriodEnd(req.getReportPeriodEnd())
                .status("GENERATING")
                .totalRecords(0L)
                .build();

        report = analyticsReportRepository.save(report);

        try {
            long totalRecords = 0L;
            StringBuilder reportDataBuilder = new StringBuilder();
            reportDataBuilder.append("{");
            reportDataBuilder.append("\"reportType\":\"").append(req.getReportType()).append("\",");
            reportDataBuilder.append("\"periodStart\":\"").append(req.getReportPeriodStart()).append("\",");
            reportDataBuilder.append("\"periodEnd\":\"").append(req.getReportPeriodEnd()).append("\",");

            if ("BOOKING".equals(req.getReportType()) || "REVENUE".equals(req.getReportType())) {
                try {
                    Object bookingData = bookingAnalyticsClient.getAllBookings(0, 100);
                    totalRecords += 1;
                    reportDataBuilder.append("\"bookingDataFetched\":true,");
                } catch (Exception e) {
                    log.warn("Could not fetch booking data: {}", e.getMessage());
                    reportDataBuilder.append("\"bookingDataFetched\":false,");
                }
            }

            if ("INVENTORY".equals(req.getReportType()) || "PARTNER_PERFORMANCE".equals(req.getReportType())) {
                try {
                    Object partnerData = partnerAnalyticsClient.getAllPartners(0, 100);
                    Object packageData = partnerAnalyticsClient.getAllPackages(0, 100);
                    totalRecords += 1;
                    reportDataBuilder.append("\"partnerDataFetched\":true,");
                } catch (Exception e) {
                    log.warn("Could not fetch partner data: {}", e.getMessage());
                    reportDataBuilder.append("\"partnerDataFetched\":false,");
                }
            }

            reportDataBuilder.append("\"totalRecords\":").append(totalRecords);
            reportDataBuilder.append("}");

            report.setStatus("COMPLETED");
            report.setTotalRecords(totalRecords);
            report.setReportData(reportDataBuilder.toString());

        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage());
            report.setStatus("FAILED");
            report.setReportData("{\"error\":\"" + e.getMessage() + "\"}");
        }

        return mapToDto(analyticsReportRepository.save(report));
    }

    public DashboardSummaryDto getDashboardSummary() {
        long totalBookings = 0L;
        long totalPartners = 0L;
        long totalPackages = 0L;

        try {
            bookingAnalyticsClient.getAllBookings(0, 1);
            totalBookings = 1L;
        } catch (Exception e) {
            log.warn("Booking service unavailable: {}", e.getMessage());
        }

        try {
            partnerAnalyticsClient.getAllPartners(0, 1);
            totalPartners = 1L;
        } catch (Exception e) {
            log.warn("Partner service unavailable: {}", e.getMessage());
        }

        try {
            partnerAnalyticsClient.getAllPackages(0, 1);
            totalPackages = 1L;
        } catch (Exception e) {
            log.warn("Package data unavailable: {}", e.getMessage());
        }

        List<DashboardMetricDto> metrics = dashboardMetricRepository.findAll()
                .stream().map(this::mapMetricToDto).collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalBookings(totalBookings)
                .totalRevenue(BigDecimal.ZERO)
                .totalPartners(totalPartners)
                .totalPackages(totalPackages)
                .activeUsers(0L)
                .metrics(metrics)
                .generatedAt(LocalDateTime.now().toString())
                .build();
    }

    public DashboardMetricDto upsertMetric(DashboardMetricDto dto) {
        DashboardMetric metric = dashboardMetricRepository.findByMetricName(dto.getMetricName())
                .orElse(new DashboardMetric());

        metric.setMetricName(dto.getMetricName());
        metric.setMetricValue(dto.getMetricValue());
        metric.setMetricUnit(dto.getMetricUnit());
        metric.setCategory(dto.getCategory());
        metric.setLastUpdated(LocalDateTime.now());

        return mapMetricToDto(dashboardMetricRepository.save(metric));
    }

    public List<DashboardMetricDto> getMetricsByCategory(String category) {
        return dashboardMetricRepository.findByCategory(category)
                .stream().map(this::mapMetricToDto).collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "anonymous";
    }

    private AnalyticsReportDto mapToDto(AnalyticsReport r) {
        return AnalyticsReportDto.builder()
                .id(r.getId())
                .reportName(r.getReportName())
                .reportType(r.getReportType())
                .generatedBy(r.getGeneratedBy())
                .reportPeriodStart(r.getReportPeriodStart())
                .reportPeriodEnd(r.getReportPeriodEnd())
                .totalRecords(r.getTotalRecords())
                .reportData(r.getReportData())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private DashboardMetricDto mapMetricToDto(DashboardMetric m) {
        return DashboardMetricDto.builder()
                .id(m.getId())
                .metricName(m.getMetricName())
                .metricValue(m.getMetricValue())
                .metricUnit(m.getMetricUnit())
                .category(m.getCategory())
                .lastUpdated(m.getLastUpdated())
                .build();
    }
}
