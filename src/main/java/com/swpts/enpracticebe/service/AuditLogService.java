package com.swpts.enpracticebe.service;

import java.util.Map;
import java.util.UUID;

public interface AuditLogService {

    void log(UUID adminId, String action, String entityType, UUID entityId, Map<String, Object> details);
}
