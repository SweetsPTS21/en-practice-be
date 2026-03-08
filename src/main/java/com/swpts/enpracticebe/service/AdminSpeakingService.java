package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.admin.CreateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminSpeakingTopicResponse;

import java.util.UUID;

public interface AdminSpeakingService {

    PageResponse<AdminSpeakingTopicResponse> listTopics(SpeakingTopicFilterRequest request);

    AdminSpeakingTopicResponse getTopicDetail(UUID topicId);

    AdminSpeakingTopicResponse createTopic(CreateSpeakingTopicRequest request);

    AdminSpeakingTopicResponse updateTopic(UUID topicId, UpdateSpeakingTopicRequest request);

    void deleteTopic(UUID topicId);

    void togglePublish(UUID topicId, boolean published);
}
