package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.writing.SubmitWritingRequest;
import com.swpts.enpracticebe.dto.request.writing.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingSubmissionResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskListResponse;
import com.swpts.enpracticebe.dto.response.writing.WritingTaskResponse;

import java.util.UUID;

public interface WritingService {

    PageResponse<WritingTaskListResponse> getWritingTasks(WritingTaskFilterRequest request);

    WritingTaskResponse getWritingTaskDetail(UUID taskId);

    WritingSubmissionResponse submitEssay(UUID taskId, SubmitWritingRequest request);

    WritingSubmissionResponse getSubmissionDetail(UUID submissionId);

    PageResponse<WritingSubmissionResponse> getSubmissionHistory(int page, int size);
}
