package com.swpts.enpracticebe.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private UUID adminId;
    private String adminName;
    private String action;
    private String entityType;
    private UUID entityId;
    private Map<String, Object> details;
    private Instant createdAt;
}
