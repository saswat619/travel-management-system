package com.travel.compliance.repository;

import com.travel.compliance.entity.ComplianceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {

    Page<ComplianceReport> findByScope(String scope, Pageable pageable);

    Page<ComplianceReport> findByReportType(String reportType, Pageable pageable);

    Page<ComplianceReport> findByStatus(String status, Pageable pageable);

    Page<ComplianceReport> findByGeneratedBy(String generatedBy, Pageable pageable);

    List<ComplianceReport> findByGeneratedDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT r FROM ComplianceReport r WHERE r.periodStart >= :start AND r.periodEnd <= :end")
    List<ComplianceReport> findByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(r) FROM ComplianceReport r WHERE r.scope = :scope AND r.status != 'ARCHIVED'")
    Long countActiveByScope(@Param("scope") String scope);
}
