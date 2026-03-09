package com.swpts.enpracticebe.service;

import java.util.UUID;

public interface UserActivityLogService {
    
    void logActivity(UUID userId, String activityType, UUID entityId, String entityName);
}
