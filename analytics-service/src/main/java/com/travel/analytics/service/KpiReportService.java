package com.travel.analytics.service;

import com.travel.analytics.client.BookingAnalyticsClient;
import com.travel.analytics.client.PartnerAnalyticsClient;
import com.travel.analytics.dto.KpiReportDto;
import com.travel.analytics.dto.KpiReportRequest;
import com.travel.analytics.entity.KpiReport;
import com.travel.analytics.exception.ResourceNotFoundException;
import com.travel.analytics.repository.KpiReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KpiReportService {

    private final KpiReportRepository kpiReportRepository;
    private final BookingAnalyticsClient bookingClient;
    private final PartnerAnalyticsClient partnerClient;

    public Page<KpiReportDto> getAllKpiReports(Pageable pageable) {
        return kpiReportRepository.findAll(pageable).map(this::mapToDto);
    }

    public KpiReportDto getKpiReportById(Long id) {
        return kpiReportRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("KpiReport", "id", id));
    }

    public Page<KpiReportDto> getKpiReportsByScope(String scope, Pageable pageable) {
        return kpiReportRepository.findByScope(scope, pageable).map(this::mapToDto);
    }

    public Page<KpiReportDto> getKpiReportsByPeriod(String period, Pageable pageable) {
        return kpiReportRepository.findByPeriod(period, pageable).map(this::mapToDto);
    }

    public List<KpiReportDto> getKpiReportsByDateRange(LocalDate start, LocalDate end) {
        return kpiReportRepository.findByPeriodRange(start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<KpiReportDto> getLatestKpiByScope(String scope) {
        return kpiReportRepository.findLatestByScope(scope, PageRequest.of(0, 5))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public KpiReportDto generateKpiReport(KpiReportRequest req) {
        log.info("Generating KPI report: scope={}, period={}", req.getScope(), req.getPeriod());
        String currentUser = getCurrentUsername();

        // Try to fetch live data from other services; fall back to request values
        Long bookingVolume = req.getBookingVolume();
        Long activePartners = req.getActivePartners();

        try {
            Object bookingData = bookingClient.getAllBookings(0, 1);
            if (bookingData != null && bookingVolume == null) {
                bookingVolume = 0L; // placeholder - real impl would parse total elements
            }
        } catch (Exception e) {
            log.warn("Could not fetch booking data for KPI: {}", e.getMessage());
        }

        try {
            Object partnerData = partnerClient.getAllPartners(0, 1);
            if (partnerData != null && activePartners == null) {
                activePartners = 0L;
            }
        } catch (Exception e) {
            log.warn("Could not fetch partner data for KPI: {}", e.getMessage());
        }

        BigDecimal cancellationRate = BigDecimal.ZERO;
        if (bookingVolume != null && bookingVolume > 0 && req.getCancellationCount() != null) {
            cancellationRate = new BigDecimal(req.getCancellationCount())
                    .divide(new BigDecimal(bookingVolume), 4, java.math.RoundingMode.HALF_UP);
        }

        String metrics = String.format(
            "{\"scope\":\"%s\",\"period\":\"%s\",\"bookingVolume\":%d,\"cancellationRate\":%.4f,\"totalRevenue\":%.2f,\"avgSpendPerTraveler\":%.2f}",
            req.getScope(), req.getPeriod(),
            bookingVolume != null ? bookingVolume : 0,
            cancellationRate,
            req.getTotalRevenue() != null ? req.getTotalRevenue() : BigDecimal.ZERO,
            req.getAvgSpendPerTraveler() != null ? req.getAvgSpendPerTraveler() : BigDecimal.ZERO
        );

        KpiReport report = KpiReport.builder()
                .scope(req.getScope())
                .metrics(metrics)
                .generatedDate(LocalDate.now())
                .generatedBy(currentUser)
                .period(req.getPeriod())
                .periodStart(req.getPeriodStart())
                .periodEnd(req.getPeriodEnd())
                .bookingVolume(bookingVolume)
                .cancellationCount(req.getCancellationCount())
                .cancellationRate(cancellationRate)
                .totalRevenue(req.getTotalRevenue())
                .avgSpendPerTraveler(req.getAvgSpendPerTraveler())
                .totalTravelers(req.getTotalTravelers())
                .activePartners(activePartners)
                .status("DRAFT")
                .notes(req.getNotes())
                .build();

        return mapToDto(kpiReportRepository.save(report));
    }

    @Transactional
    public KpiReportDto publishKpiReport(Long id) {
        KpiReport report = kpiReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KpiReport", "id", id));
        report.setStatus("PUBLISHED");
        return mapToDto(kpiReportRepository.save(report));
    }

    public Double getAvgCancellationRate(String period) {
        return kpiReportRepository.getAvgCancellationRateByPeriod(period);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private KpiReportDto mapToDto(KpiReport k) {
        return KpiReportDto.builder()
                .id(k.getId())
                .scope(k.getScope())
                .metrics(k.getMetrics())
                .generatedDate(k.getGeneratedDate())
                .generatedBy(k.getGeneratedBy())
                .period(k.getPeriod())
                .periodStart(k.getPeriodStart())
                .periodEnd(k.getPeriodEnd())
                .bookingVolume(k.getBookingVolume())
                .cancellationCount(k.getCancellationCount())
                .cancellationRate(k.getCancellationRate())
                .totalRevenue(k.getTotalRevenue())
                .avgSpendPerTraveler(k.getAvgSpendPerTraveler())
                .totalTravelers(k.getTotalTravelers())
                .activePartners(k.getActivePartners())
                .status(k.getStatus())
                .notes(k.getNotes())
                .createdAt(k.getCreatedAt())
                .build();
    }
}
