package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.constant.ActivityType;

import java.util.UUID;

public interface UserActivityLogService {
    
    void logActivity(UUID userId, ActivityType activityType, UUID entityId, String entityName);
}
