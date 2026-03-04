package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.AdminIeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.CreateIeltsTestRequest;
import com.swpts.enpracticebe.dto.request.UpdateIeltsTestRequest;
import com.swpts.enpracticebe.dto.response.AdminIeltsTestDetailResponse;
import com.swpts.enpracticebe.dto.response.AdminIeltsTestListResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;

import java.util.UUID;

public interface AdminIeltsTestService {

    PageResponse<AdminIeltsTestListResponse> listTests(AdminIeltsTestFilterRequest request);

    AdminIeltsTestDetailResponse getTestDetail(UUID testId);

    AdminIeltsTestDetailResponse createTest(CreateIeltsTestRequest request);

    AdminIeltsTestDetailResponse updateTest(UUID testId, UpdateIeltsTestRequest request);

    void deleteTest(UUID testId);

    void togglePublish(UUID testId, boolean published);
}
