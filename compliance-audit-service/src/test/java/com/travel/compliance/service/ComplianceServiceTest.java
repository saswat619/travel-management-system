package com.travel.compliance.service;

import com.travel.compliance.dto.ComplianceRecordDto;
import com.travel.compliance.dto.ComplianceRecordRequest;
import com.travel.compliance.entity.ComplianceRecord;
import com.travel.compliance.exception.ResourceNotFoundException;
import com.travel.compliance.repository.ComplianceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceRecordRepository complianceRecordRepository;

    @InjectMocks
    private ComplianceService complianceService;

    @Test
    void testCreateRecord() {
        ComplianceRecordRequest request = new ComplianceRecordRequest();
        request.setRecordType("GDPR");
        request.setTitle("Data Breach Detected");
        request.setSeverity("HIGH");

        ComplianceRecord savedRecord = ComplianceRecord.builder()
                .id(1L)
                .recordType("GDPR")
                .title("Data Breach Detected")
                .severity("HIGH")
                .status("OPEN")
                .build();

        when(complianceRecordRepository.save(any(ComplianceRecord.class))).thenReturn(savedRecord);

        ComplianceRecordDto result = complianceService.createRecord(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("OPEN");
        assertThat(result.getTitle()).isEqualTo("Data Breach Detected");
    }

    @Test
    void testUpdateRecordStatus_SetsResolvedAtWhenResolved() {
        ComplianceRecord existingRecord = ComplianceRecord.builder()
                .id(1L)
                .title("Test Record")
                .status("OPEN")
                .severity("HIGH")
                .build();

        ComplianceRecord updatedRecord = ComplianceRecord.builder()
                .id(1L)
                .title("Test Record")
                .status("RESOLVED")
                .severity("HIGH")
                .resolvedBy("admin")
                .build();

        when(complianceRecordRepository.findById(1L)).thenReturn(Optional.of(existingRecord));
        when(complianceRecordRepository.save(any(ComplianceRecord.class))).thenReturn(updatedRecord);

        ComplianceRecordDto result = complianceService.updateRecordStatus(1L, "RESOLVED", "admin");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        assertThat(result.getResolvedBy()).isEqualTo("admin");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get record by ID returns correct DTO
    // -----------------------------------------------------------------------
    @Test
    void testGetRecordById_Found_ReturnsDto() {
        ComplianceRecord record = ComplianceRecord.builder()
                .id(1L)
                .recordType("GDPR")
                .title("Data Breach")
                .severity("HIGH")
                .status("OPEN")
                .build();

        when(complianceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        ComplianceRecordDto result = complianceService.getRecordById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRecordType()).isEqualTo("GDPR");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: get record by ID throws exception when not found
    // -----------------------------------------------------------------------
    @Test
    void testGetRecordById_NotFound_ThrowsException() {
        when(complianceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: update record status throws exception when record not found
    // -----------------------------------------------------------------------
    @Test
    void testUpdateRecordStatus_RecordNotFound_ThrowsException() {
        when(complianceRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.updateRecordStatus(99L, "RESOLVED", "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get open records returns OPEN and IN_REVIEW records
    // -----------------------------------------------------------------------
    @Test
    void testGetOpenRecords_ReturnsOpenAndInReviewRecords() {
        ComplianceRecord openRecord = ComplianceRecord.builder()
                .id(1L).title("Open Issue").status("OPEN").severity("MEDIUM").build();
        ComplianceRecord inReviewRecord = ComplianceRecord.builder()
                .id(2L).title("Review Issue").status("IN_REVIEW").severity("LOW").build();

        when(complianceRecordRepository.findByStatusIn(List.of("OPEN", "IN_REVIEW")))
                .thenReturn(List.of(openRecord, inReviewRecord));

        List<ComplianceRecordDto> result = complianceService.getOpenRecords();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ComplianceRecordDto::getStatus)
                .containsExactlyInAnyOrder("OPEN", "IN_REVIEW");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get severity statistics returns counts for all severities
    // -----------------------------------------------------------------------
    @Test
    void testGetSeverityStatistics_ReturnsAllSeverityCounts() {
        when(complianceRecordRepository.countOpenBySeverity("LOW")).thenReturn(1L);
        when(complianceRecordRepository.countOpenBySeverity("MEDIUM")).thenReturn(3L);
        when(complianceRecordRepository.countOpenBySeverity("HIGH")).thenReturn(2L);
        when(complianceRecordRepository.countOpenBySeverity("CRITICAL")).thenReturn(0L);

        Map<String, Long> stats = complianceService.getSeverityStatistics();

        assertThat(stats).hasSize(4);
        assertThat(stats.get("LOW")).isEqualTo(1L);
        assertThat(stats.get("MEDIUM")).isEqualTo(3L);
        assertThat(stats.get("HIGH")).isEqualTo(2L);
        assertThat(stats.get("CRITICAL")).isEqualTo(0L);
    }

    // -----------------------------------------------------------------------
    // POSITIVE: update record status to CLOSED also sets resolvedBy
    // -----------------------------------------------------------------------
    @Test
    void testUpdateRecordStatus_ToClosed_SetsResolvedBy() {
        ComplianceRecord existingRecord = ComplianceRecord.builder()
                .id(1L).title("CLOSED Issue").status("IN_REVIEW").severity("LOW").build();

        ComplianceRecord closedRecord = ComplianceRecord.builder()
                .id(1L).title("CLOSED Issue").status("CLOSED").severity("LOW")
                .resolvedBy("compliance_officer").build();

        when(complianceRecordRepository.findById(1L)).thenReturn(Optional.of(existingRecord));
        when(complianceRecordRepository.save(any(ComplianceRecord.class))).thenReturn(closedRecord);

        ComplianceRecordDto result = complianceService.updateRecordStatus(1L, "CLOSED", "compliance_officer");

        assertThat(result.getStatus()).isEqualTo("CLOSED");
        assertThat(result.getResolvedBy()).isEqualTo("compliance_officer");
        verify(complianceRecordRepository).save(any(ComplianceRecord.class));
    }
}
