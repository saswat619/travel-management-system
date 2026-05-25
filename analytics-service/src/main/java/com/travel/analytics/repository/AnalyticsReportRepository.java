package com.travel.analytics.repository;

import com.travel.analytics.entity.AnalyticsReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, Long> {

    Page<AnalyticsReport> findByReportType(String type, Pageable pageable);

    Page<AnalyticsReport> findByGeneratedBy(String user, Pageable pageable);

    Page<AnalyticsReport> findByReportPeriodStartBetween(LocalDate start, LocalDate end, Pageable pageable);

    Optional<AnalyticsReport> findByReportName(String name);
}
