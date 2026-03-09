package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AuditLogResponse;

public interface AdminAuditLogQueryService {

    PageResponse<AuditLogResponse> getAuditLogs(int page, int size, String action, String entityType);
}
