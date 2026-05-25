package com.travel.compliance.service;

import com.travel.compliance.dto.ComplianceRecordDto;
import com.travel.compliance.dto.ComplianceRecordRequest;
import com.travel.compliance.entity.ComplianceRecord;
import com.travel.compliance.exception.ResourceNotFoundException;
import com.travel.compliance.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComplianceService {

    private final ComplianceRecordRepository complianceRecordRepository;

    public Page<ComplianceRecordDto> getAllRecords(Pageable pageable) {
        return complianceRecordRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ComplianceRecordDto> getRecordsBySeverity(String severity, Pageable pageable) {
        return complianceRecordRepository.findBySeverity(severity, pageable).map(this::mapToDto);
    }

    public Page<ComplianceRecordDto> getRecordsByStatus(String status, Pageable pageable) {
        return complianceRecordRepository.findByStatus(status, pageable).map(this::mapToDto);
    }

    public List<ComplianceRecordDto> getOpenRecords() {
        return complianceRecordRepository.findByStatusIn(List.of("OPEN", "IN_REVIEW"))
                .stream().map(this::mapToDto).toList();
    }

    public ComplianceRecordDto getRecordById(Long id) {
        ComplianceRecord record = complianceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord not found with id: " + id));
        return mapToDto(record);
    }

    public ComplianceRecordDto createRecord(ComplianceRecordRequest req) {
        ComplianceRecord record = ComplianceRecord.builder()
                .recordType(req.getRecordType())
                .title(req.getTitle())
                .description(req.getDescription())
                .affectedResource(req.getAffectedResource())
                .affectedUserId(req.getAffectedUserId())
                .severity(req.getSeverity())
                .status("OPEN")
                .notes(req.getNotes())
                .build();
        return mapToDto(complianceRecordRepository.save(record));
    }

    public ComplianceRecordDto updateRecordStatus(Long id, String status, String resolvedBy) {
        ComplianceRecord record = complianceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceRecord not found with id: " + id));
        record.setStatus(status);
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            record.setResolvedAt(LocalDateTime.now());
            record.setResolvedBy(resolvedBy);
        }
        return mapToDto(complianceRecordRepository.save(record));
    }

    public Map<String, Long> getSeverityStatistics() {
        Map<String, Long> stats = new HashMap<>();
        for (String severity : List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")) {
            stats.put(severity, complianceRecordRepository.countOpenBySeverity(severity));
        }
        return stats;
    }

    private ComplianceRecordDto mapToDto(ComplianceRecord c) {
        return ComplianceRecordDto.builder()
                .id(c.getId())
                .recordType(c.getRecordType())
                .title(c.getTitle())
                .description(c.getDescription())
                .affectedResource(c.getAffectedResource())
                .affectedUserId(c.getAffectedUserId())
                .severity(c.getSeverity())
                .status(c.getStatus())
                .resolvedBy(c.getResolvedBy())
                .resolvedAt(c.getResolvedAt())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
