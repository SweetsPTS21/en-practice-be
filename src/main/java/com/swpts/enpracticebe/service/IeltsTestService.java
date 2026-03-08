package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.listening.IeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.listening.SubmitTestRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.listening.*;

import java.util.UUID;

public interface IeltsTestService {

    PageResponse<IeltsTestListResponse> getTests(IeltsTestFilterRequest request);

    IeltsTestDetailResponse getTestDetail(UUID testId);

    StartTestResponse startTest(UUID testId);

    SubmitTestResponse submitTest(SubmitTestRequest request);

    PageResponse<TestAttemptHistoryResponse> getAttemptHistory(int page, int size);

    SubmitTestResponse getAttemptDetail(UUID attemptId);
}
