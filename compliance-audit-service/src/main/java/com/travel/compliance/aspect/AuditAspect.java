package com.travel.compliance.aspect;

import com.travel.compliance.annotation.Auditable;
import com.travel.compliance.entity.AuditLog;
import com.travel.compliance.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(com.travel.compliance.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        String userId = getCurrentUserId();
        boolean success = true;
        String errorMsg = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            success = false;
            errorMsg = ex.getMessage();
            throw ex;
        } finally {
            long execTime = System.currentTimeMillis() - startTime;
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(auditable.action().isEmpty() ? method.getName() : auditable.action())
                    .resource(auditable.resource().isEmpty() ? joinPoint.getTarget().getClass().getSimpleName() : auditable.resource())
                    .description(auditable.description())
                    .executionTimeMs(execTime)
                    .success(success)
                    .errorMessage(errorMsg)
                    .timestamp(LocalDateTime.now())
                    .build();
            try {
                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                log.warn("Failed to save audit log: {}", e.getMessage());
            }
        }
        return result;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }
}
