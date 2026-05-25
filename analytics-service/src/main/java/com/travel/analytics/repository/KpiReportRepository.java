package com.travel.analytics.repository;

import com.travel.analytics.entity.KpiReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KpiReportRepository extends JpaRepository<KpiReport, Long> {

    Page<KpiReport> findByScope(String scope, Pageable pageable);

    Page<KpiReport> findByPeriod(String period, Pageable pageable);

    Page<KpiReport> findByStatus(String status, Pageable pageable);

    List<KpiReport> findByGeneratedDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT k FROM KpiReport k WHERE k.periodStart >= :start AND k.periodEnd <= :end ORDER BY k.generatedDate DESC")
    List<KpiReport> findByPeriodRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT k FROM KpiReport k WHERE k.scope = :scope ORDER BY k.generatedDate DESC")
    List<KpiReport> findLatestByScope(@Param("scope") String scope, Pageable pageable);

    @Query("SELECT AVG(k.cancellationRate) FROM KpiReport k WHERE k.period = :period")
    Double getAvgCancellationRateByPeriod(@Param("period") String period);
}
