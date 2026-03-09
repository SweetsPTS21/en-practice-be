package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.entity.AuditLog;
import com.swpts.enpracticebe.repository.AuditLogRepository;
import com.swpts.enpracticebe.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @CacheEvict(value = "adminAuditLogList", allEntries = true)
    public void log(UUID adminId, String action, String entityType, UUID entityId, Map<String, Object> details) {
        AuditLog auditLog = AuditLog.builder()
                .adminId(adminId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }
}
