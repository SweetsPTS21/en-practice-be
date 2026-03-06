package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.AdminIeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.CreateIeltsTestRequest;
import com.swpts.enpracticebe.dto.request.UpdateIeltsTestRequest;
import com.swpts.enpracticebe.dto.response.AdminIeltsTestListResponse;
import com.swpts.enpracticebe.dto.response.IeltsTestDetailResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;

import java.util.UUID;

public interface AdminIeltsTestService {

    PageResponse<AdminIeltsTestListResponse> listTests(AdminIeltsTestFilterRequest request);

    IeltsTestDetailResponse getTestDetail(UUID testId);

    IeltsTestDetailResponse createTest(CreateIeltsTestRequest request);

    IeltsTestDetailResponse updateTest(UUID testId, UpdateIeltsTestRequest request);

    void deleteTest(UUID testId);

    void togglePublish(UUID testId, boolean published);
}
