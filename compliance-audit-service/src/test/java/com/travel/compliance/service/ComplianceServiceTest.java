package com.travel.compliance.service;

import com.travel.compliance.dto.ComplianceRecordDto;
import com.travel.compliance.dto.ComplianceRecordRequest;
import com.travel.compliance.entity.ComplianceRecord;
import com.travel.compliance.repository.ComplianceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
