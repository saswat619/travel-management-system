package com.travel.analytics.service;

import com.travel.analytics.client.BookingAnalyticsClient;
import com.travel.analytics.client.PartnerAnalyticsClient;
import com.travel.analytics.dto.AnalyticsReportDto;
import com.travel.analytics.dto.DashboardMetricDto;
import com.travel.analytics.dto.DashboardSummaryDto;
import com.travel.analytics.dto.ReportRequest;
import com.travel.analytics.entity.AnalyticsReport;
import com.travel.analytics.entity.DashboardMetric;
import com.travel.analytics.repository.AnalyticsReportRepository;
import com.travel.analytics.repository.DashboardMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsReportRepository analyticsReportRepository;

    @Mock
    private DashboardMetricRepository dashboardMetricRepository;

    @Mock
    private BookingAnalyticsClient bookingAnalyticsClient;

    @Mock
    private PartnerAnalyticsClient partnerAnalyticsClient;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void testGenerateReport() {
        ReportRequest request = new ReportRequest();
        request.setReportName("Test Report");
        request.setReportType("BOOKING");
        request.setReportPeriodStart(LocalDate.of(2024, 1, 1));
        request.setReportPeriodEnd(LocalDate.of(2024, 1, 31));

        AnalyticsReport initialReport = AnalyticsReport.builder()
                .id(1L)
                .reportName("Test Report")
                .reportType("BOOKING")
                .status("GENERATING")
                .totalRecords(0L)
                .build();

        AnalyticsReport completedReport = AnalyticsReport.builder()
                .id(1L)
                .reportName("Test Report")
                .reportType("BOOKING")
                .status("COMPLETED")
                .totalRecords(1L)
                .reportData("{\"status\":\"completed\"}")
                .build();

        when(analyticsReportRepository.save(any(AnalyticsReport.class)))
                .thenReturn(initialReport)
                .thenReturn(completedReport);

        AnalyticsReportDto result = analyticsService.generateReport(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(analyticsReportRepository, times(2)).save(any(AnalyticsReport.class));
    }

    @Test
    void testGetDashboardSummary_WhenFeignClientThrowsException_ReturnsSummaryWithZeroCounts() {
        when(bookingAnalyticsClient.getAllBookings(0, 1))
                .thenThrow(new RuntimeException("Booking service unavailable"));
        when(partnerAnalyticsClient.getAllPartners(0, 1))
                .thenThrow(new RuntimeException("Partner service unavailable"));
        when(partnerAnalyticsClient.getAllPackages(0, 1))
                .thenThrow(new RuntimeException("Package service unavailable"));
        when(dashboardMetricRepository.findAll()).thenReturn(List.of());

        DashboardSummaryDto result = analyticsService.getDashboardSummary();

        assertThat(result).isNotNull();
        assertThat(result.getTotalBookings()).isEqualTo(0L);
        assertThat(result.getTotalPartners()).isEqualTo(0L);
        assertThat(result.getTotalPackages()).isEqualTo(0L);
        assertThat(result.getMetrics()).isEmpty();
    }

    @Test
    void testUpsertMetric_NewMetricCreatedWhenNotFound() {
        DashboardMetricDto dto = new DashboardMetricDto();
        dto.setMetricName("TOTAL_BOOKINGS");
        dto.setMetricValue(BigDecimal.valueOf(100));
        dto.setMetricUnit("count");
        dto.setCategory("BOOKING");

        DashboardMetric savedMetric = DashboardMetric.builder()
                .id(1L)
                .metricName("TOTAL_BOOKINGS")
                .metricValue(BigDecimal.valueOf(100))
                .metricUnit("count")
                .category("BOOKING")
                .build();

        when(dashboardMetricRepository.findByMetricName("TOTAL_BOOKINGS")).thenReturn(Optional.empty());
        when(dashboardMetricRepository.save(any(DashboardMetric.class))).thenReturn(savedMetric);

        DashboardMetricDto result = analyticsService.upsertMetric(dto);

        assertThat(result).isNotNull();
        assertThat(result.getMetricName()).isEqualTo("TOTAL_BOOKINGS");
        assertThat(result.getId()).isEqualTo(1L);
        verify(dashboardMetricRepository).save(any(DashboardMetric.class));
    }
}
