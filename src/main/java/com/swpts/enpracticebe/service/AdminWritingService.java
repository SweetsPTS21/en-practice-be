package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.CreateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateWritingTaskRequest;
import com.swpts.enpracticebe.dto.request.writing.WritingTaskFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminWritingTaskResponse;

import java.util.UUID;

public interface AdminWritingService {

    PageResponse<AdminWritingTaskResponse> listTasks(WritingTaskFilterRequest request);

    AdminWritingTaskResponse getTaskDetail(UUID taskId);

    AdminWritingTaskResponse createTask(CreateWritingTaskRequest request);

    AdminWritingTaskResponse updateTask(UUID taskId, UpdateWritingTaskRequest request);

    void deleteTask(UUID taskId);

    void togglePublish(UUID taskId, boolean published);
}
