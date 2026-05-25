package com.travel.compliance.repository;

import com.travel.compliance.entity.ComplianceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

    Page<ComplianceRecord> findBySeverity(String severity, Pageable pageable);

    Page<ComplianceRecord> findByStatus(String status, Pageable pageable);

    Page<ComplianceRecord> findByRecordType(String recordType, Pageable pageable);

    List<ComplianceRecord> findByStatusIn(List<String> statuses);

    @Query("SELECT COUNT(c) FROM ComplianceRecord c WHERE c.severity = :severity AND c.status != 'CLOSED'")
    Long countOpenBySeverity(@Param("severity") String severity);
}
