package com.swpts.enpracticebe.mapper.admin;

import com.swpts.enpracticebe.dto.response.admin.RecentActivityResponse;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.entity.UserActivityLog;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class RecentActivityMapper {
    public RecentActivityResponse toDto(UserActivityLog from, String userName) {
        return RecentActivityResponse.builder()
                .userId(from.getUserId())
                .userName(userName)
                .activityType(from.getActivityType())
                .entityId(from.getEntityId())
                .entityName(from.getEntityName())
                .createdAt(from.getCreatedAt())
                .build();
    }

    public List<RecentActivityResponse> toDtoList(List<UserActivityLog> logs, Map<UUID, User> userMap) {
        return logs.stream()
                .map(log -> {
                    String userName = Optional.ofNullable(userMap.get(log.getUserId()))
                            .map(User::getDisplayName)
                            .orElse("Unknown User");
                    return toDto(log, userName);
                })
                .toList();
    }
}
