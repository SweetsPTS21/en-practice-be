package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AuditLogResponse;
import com.swpts.enpracticebe.entity.AuditLog;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.AuditLogRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.AdminAuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAuditLogQueryServiceImpl implements AdminAuditLogQueryService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "adminAuditLogList", key = "#page + '-' + #size + '-' + #action + '-' + #entityType")
    public PageResponse<AuditLogResponse> getAuditLogs(int page, int size, String action, String entityType) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> result;
        if (action != null && entityType != null) {
            result = auditLogRepository.findByActionAndEntityType(action, entityType, pageable);
        } else if (action != null) {
            result = auditLogRepository.findByAction(action, pageable);
        } else if (entityType != null) {
            result = auditLogRepository.findByEntityType(entityType, pageable);
        } else {
            result = auditLogRepository.findAll(pageable);
        }

        var items = result.getContent().stream().map(log -> {
            String adminName = userRepository.findById(log.getAdminId())
                    .map(User::getDisplayName)
                    .orElse("Unknown");
            return AuditLogResponse.builder()
                    .id(log.getId())
                    .adminId(log.getAdminId())
                    .adminName(adminName)
                    .action(log.getAction())
                    .entityType(log.getEntityType())
                    .entityId(log.getEntityId())
                    .details(log.getDetails())
                    .createdAt(log.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<AuditLogResponse>builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .items(items)
                .build();
    }
}
