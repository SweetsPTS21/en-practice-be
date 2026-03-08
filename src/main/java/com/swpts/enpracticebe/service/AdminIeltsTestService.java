package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.AdminIeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.admin.CreateIeltsTestRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateIeltsTestRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminIeltsTestListResponse;
import com.swpts.enpracticebe.dto.response.listening.IeltsTestDetailResponse;

import java.util.UUID;

public interface AdminIeltsTestService {

    PageResponse<AdminIeltsTestListResponse> listTests(AdminIeltsTestFilterRequest request);

    IeltsTestDetailResponse getTestDetail(UUID testId);

    IeltsTestDetailResponse createTest(CreateIeltsTestRequest request);

    IeltsTestDetailResponse updateTest(UUID testId, UpdateIeltsTestRequest request);

    void deleteTest(UUID testId);

    void togglePublish(UUID testId, boolean published);
}
