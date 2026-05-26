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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    // -----------------------------------------------------------------------
    // POSITIVE: get report by ID returns correct DTO
    // -----------------------------------------------------------------------
    @Test
    void testGetReportById_Found_ReturnsDto() {
        AnalyticsReport report = AnalyticsReport.builder()
                .id(1L)
                .reportName("Monthly Report")
                .reportType("BOOKING")
                .status("COMPLETED")
                .totalRecords(50L)
                .build();

        when(analyticsReportRepository.findById(1L)).thenReturn(Optional.of(report));

        AnalyticsReportDto result = analyticsService.getReportById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReportName()).isEqualTo("Monthly Report");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: get report by ID throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testGetReportById_NotFound_ThrowsException() {
        when(analyticsReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getReportById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: upsert metric updates existing metric when found
    // -----------------------------------------------------------------------
    @Test
    void testUpsertMetric_ExistingMetricUpdated() {
        DashboardMetricDto dto = new DashboardMetricDto();
        dto.setMetricName("TOTAL_REVENUE");
        dto.setMetricValue(BigDecimal.valueOf(50000));
        dto.setMetricUnit("USD");
        dto.setCategory("FINANCE");

        DashboardMetric existingMetric = DashboardMetric.builder()
                .id(5L)
                .metricName("TOTAL_REVENUE")
                .metricValue(BigDecimal.valueOf(40000))
                .metricUnit("USD")
                .category("FINANCE")
                .build();

        DashboardMetric updatedMetric = DashboardMetric.builder()
                .id(5L)
                .metricName("TOTAL_REVENUE")
                .metricValue(BigDecimal.valueOf(50000))
                .metricUnit("USD")
                .category("FINANCE")
                .lastUpdated(LocalDateTime.now())
                .build();

        when(dashboardMetricRepository.findByMetricName("TOTAL_REVENUE")).thenReturn(Optional.of(existingMetric));
        when(dashboardMetricRepository.save(any(DashboardMetric.class))).thenReturn(updatedMetric);

        DashboardMetricDto result = analyticsService.upsertMetric(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getMetricValue()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        verify(dashboardMetricRepository).save(existingMetric);
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get metrics by category returns matching metrics
    // -----------------------------------------------------------------------
    @Test
    void testGetMetricsByCategory_ReturnsMatchingMetrics() {
        DashboardMetric metric1 = DashboardMetric.builder()
                .id(1L).metricName("TOTAL_BOOKINGS").metricValue(BigDecimal.valueOf(200))
                .metricUnit("count").category("BOOKING").build();
        DashboardMetric metric2 = DashboardMetric.builder()
                .id(2L).metricName("CONFIRMED_BOOKINGS").metricValue(BigDecimal.valueOf(180))
                .metricUnit("count").category("BOOKING").build();

        when(dashboardMetricRepository.findByCategory("BOOKING"))
                .thenReturn(List.of(metric1, metric2));

        List<DashboardMetricDto> results = analyticsService.getMetricsByCategory("BOOKING");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(DashboardMetricDto::getCategory)
                .containsOnly("BOOKING");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: dashboard summary returns non-zero counts when services available
    // -----------------------------------------------------------------------
    @Test
    void testGetDashboardSummary_WhenServicesAvailable_ReturnsCounts() {
        when(bookingAnalyticsClient.getAllBookings(0, 1)).thenReturn(new Object());
        when(partnerAnalyticsClient.getAllPartners(0, 1)).thenReturn(new Object());
        when(partnerAnalyticsClient.getAllPackages(0, 1)).thenReturn(new Object());
        when(dashboardMetricRepository.findAll()).thenReturn(List.of());

        DashboardSummaryDto result = analyticsService.getDashboardSummary();

        assertThat(result).isNotNull();
        assertThat(result.getTotalBookings()).isEqualTo(1L);
        assertThat(result.getTotalPartners()).isEqualTo(1L);
        assertThat(result.getTotalPackages()).isEqualTo(1L);
    }
}
