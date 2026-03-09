package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.UserFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserDetailResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminUserListResponse;

import java.util.UUID;

public interface AdminUserService {

    PageResponse<AdminUserListResponse> listUsers(UserFilterRequest filter);

    AdminUserDetailResponse getUserDetail(UUID userId);

    void changeRole(UUID userId, String role);

    void toggleStatus(UUID userId);
}
