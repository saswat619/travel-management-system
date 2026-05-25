package com.travel.compliance.service;

import com.travel.compliance.annotation.Auditable;
import com.travel.compliance.dto.AuditLogDto;
import com.travel.compliance.dto.AuditLogRequest;
import com.travel.compliance.entity.AuditLog;
import com.travel.compliance.repository.AuditLogRepository;
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
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogDto> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<AuditLogDto> getAuditLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public Page<AuditLogDto> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable).map(this::mapToDto);
    }

    public Page<AuditLogDto> getAuditLogsByResource(String resource, Pageable pageable) {
        return auditLogRepository.findByResource(resource, pageable).map(this::mapToDto);
    }

    public Page<AuditLogDto> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable).map(this::mapToDto);
    }

    public Page<AuditLogDto> getFailedActions(Pageable pageable) {
        return auditLogRepository.findFailedActions(pageable).map(this::mapToDto);
    }

    @Auditable(action = "CREATE", resource = "AuditLog")
    public AuditLogDto createAuditLog(AuditLogRequest req) {
        AuditLog auditLog = AuditLog.builder()
                .userId(req.getUserId())
                .action(req.getAction())
                .resource(req.getResource())
                .resourceId(req.getResourceId())
                .description(req.getDescription())
                .ipAddress(req.getIpAddress())
                .userAgent(req.getUserAgent())
                .requestMethod(req.getRequestMethod())
                .requestUri(req.getRequestUri())
                .responseStatus(req.getResponseStatus())
                .executionTimeMs(req.getExecutionTimeMs())
                .success(req.isSuccess())
                .errorMessage(req.getErrorMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return mapToDto(auditLogRepository.save(auditLog));
    }

    public Map<String, Long> getActionStatistics() {
        List<Object[]> results = auditLogRepository.countByAction();
        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : results) {
            stats.put((String) row[0], (Long) row[1]);
        }
        return stats;
    }

    private AuditLogDto mapToDto(AuditLog a) {
        return AuditLogDto.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .action(a.getAction())
                .resource(a.getResource())
                .resourceId(a.getResourceId())
                .description(a.getDescription())
                .ipAddress(a.getIpAddress())
                .requestMethod(a.getRequestMethod())
                .requestUri(a.getRequestUri())
                .responseStatus(a.getResponseStatus())
                .executionTimeMs(a.getExecutionTimeMs())
                .success(a.isSuccess())
                .errorMessage(a.getErrorMessage())
                .timestamp(a.getTimestamp())
                .build();
    }
}
