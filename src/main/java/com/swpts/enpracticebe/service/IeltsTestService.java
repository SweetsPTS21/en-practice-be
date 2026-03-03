package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.IeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.SubmitTestRequest;
import com.swpts.enpracticebe.dto.response.*;

import java.util.UUID;

public interface IeltsTestService {

    PageResponse<IeltsTestListResponse> getTests(IeltsTestFilterRequest request);

    IeltsTestDetailResponse getTestDetail(UUID testId);

    StartTestResponse startTest(UUID testId);

    SubmitTestResponse submitTest(SubmitTestRequest request);

    PageResponse<TestAttemptHistoryResponse> getAttemptHistory(int page, int size);

    SubmitTestResponse getAttemptDetail(UUID attemptId);
}
