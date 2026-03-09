package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.admin.SendNotificationRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.NotificationHistoryResponse;
import com.swpts.enpracticebe.entity.NotificationHistory;
import com.swpts.enpracticebe.constant.Role;
import com.swpts.enpracticebe.entity.User;
import com.swpts.enpracticebe.repository.NotificationHistoryRepository;
import com.swpts.enpracticebe.repository.UserRepository;
import com.swpts.enpracticebe.service.AdminNotificationService;
import com.swpts.enpracticebe.service.PushNotificationService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationServiceImpl implements AdminNotificationService {

    private final PushNotificationService pushNotificationService;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    @Override
    public void sendNotification(SendNotificationRequest request) {
        List<User> targets;
        
        if (request.getTargetType() == SendNotificationRequest.TargetType.ALL) {
            targets = userRepository.findAll().stream().filter(User::isActive).collect(Collectors.toList());
        } else {
            Role targetRole = Role.valueOf(request.getTargetRole());
            targets = userRepository.findByRole(targetRole).stream().filter(User::isActive).collect(Collectors.toList());
        }

        List<java.util.UUID> userIds = targets.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        int successCount = 0;
        if (!userIds.isEmpty()) {
            try {
                // Let the push notification service handle multicast
                com.google.firebase.messaging.BatchResponse response = pushNotificationService.sendNotificationToUsers(
                    userIds, request.getTitle(), request.getBody());
                successCount = response != null ? response.getSuccessCount() : 0;
            } catch (Exception e) {
                log.warn("Failed to send push notification to users, might be missing tokens. {}", e.getMessage());
                // Still log history
            }
        }

        NotificationHistory history = NotificationHistory.builder()
                .adminId(authUtil.getUserId())
                .title(request.getTitle())
                .body(request.getBody())
                .targetType(NotificationHistory.TargetType.valueOf(request.getTargetType().name()))
                .targetRole(request.getTargetRole())
                .recipientsCount(successCount)
                .build();
        
        notificationHistoryRepository.save(history);
    }

    @Override
    public PageResponse<NotificationHistoryResponse> getHistory(Pageable pageable) {
        Page<NotificationHistory> page = notificationHistoryRepository.findAll(pageable);
        List<NotificationHistoryResponse> items = page.getContent().stream()
                .map(history -> {
                    User admin = userRepository.findById(history.getAdminId()).orElse(null);
                    return NotificationHistoryResponse.builder()
                            .id(history.getId())
                            .adminId(history.getAdminId())
                            .adminName(admin != null ? admin.getDisplayName() : "Unknown")
                            .title(history.getTitle())
                            .body(history.getBody())
                            .targetType(history.getTargetType().name())
                            .targetRole(history.getTargetRole())
                            .recipientsCount(history.getRecipientsCount())
                            .createdAt(history.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponse.<NotificationHistoryResponse>builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }
}
