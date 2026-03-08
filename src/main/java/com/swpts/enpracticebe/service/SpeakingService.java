package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitSpeakingRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingAttemptResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingTopicListResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingTopicResponse;

import java.util.UUID;

public interface SpeakingService {

    PageResponse<SpeakingTopicListResponse> getTopics(SpeakingTopicFilterRequest request);

    SpeakingTopicResponse getTopicDetail(UUID topicId);

    SpeakingAttemptResponse submitAttempt(UUID topicId, SubmitSpeakingRequest request);

    SpeakingAttemptResponse getAttemptDetail(UUID attemptId);

    PageResponse<SpeakingAttemptResponse> getAttemptHistory(int page, int size);
}
