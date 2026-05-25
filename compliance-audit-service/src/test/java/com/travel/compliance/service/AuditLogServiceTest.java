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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
