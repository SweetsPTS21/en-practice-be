package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.SendNotificationRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.NotificationHistoryResponse;
import org.springframework.data.domain.Pageable;

public interface AdminNotificationService {
    void sendNotification(SendNotificationRequest request);
    PageResponse<NotificationHistoryResponse> getHistory(Pageable pageable);
}
