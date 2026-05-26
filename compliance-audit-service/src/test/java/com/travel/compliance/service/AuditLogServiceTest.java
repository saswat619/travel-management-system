package com.travel.compliance.service;

import com.travel.compliance.dto.AuditLogDto;
import com.travel.compliance.dto.AuditLogRequest;
import com.travel.compliance.entity.AuditLog;
import com.travel.compliance.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void testCreateAuditLog() {
        AuditLogRequest request = new AuditLogRequest();
        request.setUserId("user123");
        request.setAction("CREATE");
        request.setResource("Booking");
        request.setSuccess(true);

        AuditLog savedLog = AuditLog.builder()
                .id(1L)
                .userId("user123")
                .action("CREATE")
                .resource("Booking")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        AuditLogDto result = auditLogService.createAuditLog(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user123");
        assertThat(result.getAction()).isEqualTo("CREATE");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogsByUser() {
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId(userId)
                .action("LOGIN")
                .resource("Auth")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findByUserId(userId, pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getAuditLogsByUser(userId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void testGetActionStatistics() {
        List<Object[]> mockData = Arrays.asList(
                new Object[]{"CREATE", 10L},
                new Object[]{"DELETE", 5L},
                new Object[]{"LOGIN", 20L}
        );

        when(auditLogRepository.countByAction()).thenReturn(mockData);

        Map<String, Long> stats = auditLogService.getActionStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats).hasSize(3);
        assertThat(stats.get("CREATE")).isEqualTo(10L);
        assertThat(stats.get("LOGIN")).isEqualTo(20L);
    }

    // -----------------------------------------------------------------------
    // POSITIVE: create audit log for a failed action (success = false)
    // -----------------------------------------------------------------------
    @Test
    void testCreateAuditLog_FailedAction_StoresCorrectly() {
        AuditLogRequest request = new AuditLogRequest();
        request.setUserId("user456");
        request.setAction("DELETE");
        request.setResource("Booking");
        request.setSuccess(false);
        request.setErrorMessage("Access denied");

        AuditLog savedLog = AuditLog.builder()
                .id(2L)
                .userId("user456")
                .action("DELETE")
                .resource("Booking")
                .success(false)
                .errorMessage("Access denied")
                .timestamp(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        AuditLogDto result = auditLogService.createAuditLog(request);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Access denied");
        assertThat(result.getAction()).isEqualTo("DELETE");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get audit logs by action
    // -----------------------------------------------------------------------
    @Test
    void testGetAuditLogsByAction_ReturnsMatchingLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId("user123")
                .action("LOGIN")
                .resource("Auth")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findByAction("LOGIN", pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getAuditLogsByAction("LOGIN", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("LOGIN");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get audit logs by resource
    // -----------------------------------------------------------------------
    @Test
    void testGetAuditLogsByResource_ReturnsMatchingLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId("user123")
                .action("CREATE")
                .resource("Booking")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findByResource("Booking", pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getAuditLogsByResource("Booking", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getResource()).isEqualTo("Booking");
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get failed actions returns only failed logs
    // -----------------------------------------------------------------------
    @Test
    void testGetFailedActions_ReturnsOnlyFailedLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog failedLog = AuditLog.builder()
                .id(1L)
                .userId("user789")
                .action("DELETE")
                .resource("Payment")
                .success(false)
                .errorMessage("Unauthorized")
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(failedLog));
        when(auditLogRepository.findFailedActions(pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getFailedActions(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).isSuccess()).isFalse();
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get all audit logs returns paginated result
    // -----------------------------------------------------------------------
    @Test
    void testGetAllAuditLogs_ReturnsPaginatedResult() {
        Pageable pageable = PageRequest.of(0, 5);
        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId("user001")
                .action("VIEW")
                .resource("Report")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getAllAuditLogs(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user001");
    }

    // -----------------------------------------------------------------------
    // NEGATIVE: get action statistics returns empty map when no data
    // -----------------------------------------------------------------------
    @Test
    void testGetActionStatistics_EmptyWhenNoLogs() {
        when(auditLogRepository.countByAction()).thenReturn(Collections.emptyList());

        Map<String, Long> stats = auditLogService.getActionStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats).isEmpty();
    }

    // -----------------------------------------------------------------------
    // POSITIVE: get audit logs by date range returns logs within range
    // -----------------------------------------------------------------------
    @Test
    void testGetAuditLogsByDateRange_ReturnsLogsWithinRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        AuditLog log = AuditLog.builder()
                .id(1L)
                .userId("user123")
                .action("CREATE")
                .resource("Itinerary")
                .success(true)
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(auditLogRepository.findByTimestampBetween(start, end, pageable)).thenReturn(page);

        Page<AuditLogDto> result = auditLogService.getAuditLogsByDateRange(start, end, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user123");
    }
}
