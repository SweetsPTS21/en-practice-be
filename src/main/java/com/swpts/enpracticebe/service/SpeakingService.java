package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.request.SubmitSpeakingRequest;
import com.swpts.enpracticebe.dto.request.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.response.*;

import java.util.UUID;

public interface SpeakingService {

    PageResponse<SpeakingTopicListResponse> getTopics(SpeakingTopicFilterRequest request);

    SpeakingTopicResponse getTopicDetail(UUID topicId);

    SpeakingAttemptResponse submitAttempt(UUID topicId, SubmitSpeakingRequest request);

    SpeakingAttemptResponse getAttemptDetail(UUID attemptId);

    PageResponse<SpeakingAttemptResponse> getAttemptHistory(int page, int size);
}
