package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.entity.UserActivityLog;
import com.swpts.enpracticebe.repository.UserActivityLogRepository;
import com.swpts.enpracticebe.service.UserActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository userActivityLogRepository;

    @Override
    @Async
    @Transactional
    @CacheEvict(value = "dashboardRecentActivities", allEntries = true)
    public void logActivity(UUID userId, String activityType, UUID entityId, String entityName) {
        if (userId == null) {
            log.warn("Cannot log user activity: userId is null");
            return;
        }

        try {
            UserActivityLog activityLog = UserActivityLog.builder()
                    .userId(userId)
                    .activityType(activityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .build();

            userActivityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Failed to log user activity: userId={}, type={}", userId, activityType, e);
        }
    }
}
