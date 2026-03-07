package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.CreateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.UpdateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.AdminWritingTaskResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;

import java.util.UUID;

public interface AdminWritingService {

    PageResponse<AdminWritingTaskResponse> listTasks(WritingTaskFilterRequest request);

    AdminWritingTaskResponse getTaskDetail(UUID taskId);

    AdminWritingTaskResponse createTask(CreateWritingTaskRequest request);

    AdminWritingTaskResponse updateTask(UUID taskId, UpdateWritingTaskRequest request);

    void deleteTask(UUID taskId);

    void togglePublish(UUID taskId, boolean published);
}
