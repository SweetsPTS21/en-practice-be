package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.request.speaking.SubmitSpeakingRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingAttemptResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingTopicListResponse;
import com.swpts.enpracticebe.dto.response.speaking.SpeakingTopicResponse;
import com.swpts.enpracticebe.entity.SpeakingAttempt;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.mapper.SpeakingMapper;
import com.swpts.enpracticebe.repository.SpeakingAttemptRepository;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.SpeakingService;
import com.swpts.enpracticebe.service.UserActivityLogService;
import com.swpts.enpracticebe.constant.XpSource;
import com.swpts.enpracticebe.service.XpService;
import com.swpts.enpracticebe.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakingServiceImpl implements SpeakingService {

    private final SpeakingTopicRepository topicRepository;
    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingGradingService gradingService;
    private final AuthUtil authUtil;
    private final SpeakingMapper speakingMapper;
    private final UserActivityLogService userActivityLogService;
    private final XpService xpService;

    @Override
    public PageResponse<SpeakingTopicListResponse> getTopics(SpeakingTopicFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasPart = request.getPart() != null && !request.getPart().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();

        Page<SpeakingTopic> page;

        if (hasPart && hasDifficulty) {
            page = topicRepository.findByPartAndDifficultyAndIsPublishedTrue(
                    SpeakingTopic.Part.valueOf(request.getPart()),
                    SpeakingTopic.Difficulty.valueOf(request.getDifficulty()),
                    pageable);
        } else if (hasPart) {
            page = topicRepository.findByPartAndIsPublishedTrue(
                    SpeakingTopic.Part.valueOf(request.getPart()), pageable);
        } else if (hasDifficulty) {
            page = topicRepository.findByDifficultyAndIsPublishedTrue(
                    SpeakingTopic.Difficulty.valueOf(request.getDifficulty()), pageable);
        } else {
            page = topicRepository.findByIsPublishedTrue(pageable);
        }

        List<SpeakingTopicListResponse> items = page.getContent().stream()
                .map(speakingMapper::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<SpeakingTopicListResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    public SpeakingTopicResponse getTopicDetail(UUID topicId) {
        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Speaking topic not found: " + topicId));
        return speakingMapper.toTopicResponse(topic);
    }

    @Override
    @Transactional
    public SpeakingAttemptResponse submitAttempt(UUID topicId, SubmitSpeakingRequest request) {
        UUID userId = authUtil.getUserId();

        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Speaking topic not found: " + topicId));

        SpeakingAttempt attempt = SpeakingAttempt.builder()
                .userId(userId)
                .topicId(topicId)
                .transcript(request.getTranscript())
                .audioUrl(request.getAudioUrl())
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .status(SpeakingAttempt.AttemptStatus.SUBMITTED)
                .build();
        attempt = attemptRepository.save(attempt);

        // take the first 100 characters of the question as the entity name since Speaking topics don't have titles
        String entityName = topic.getQuestion();
        if (entityName != null && entityName.length() > 100) {
            entityName = entityName.substring(0, 97) + "...";
        }
        userActivityLogService.logActivity(userId, "SPEAKING_ATTEMPT", attempt.getId(), entityName);

        final UUID attemptId = attempt.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                gradingService.gradeAttemptAsync(attemptId, userId);
            }
        });

        xpService.earnXp(userId, XpSource.SPEAKING_PRACTICE, attemptId.toString(), 12);

        return speakingMapper.toAttemptResponse(attempt, topic);
    }

    @Override
    public SpeakingAttemptResponse getAttemptDetail(UUID attemptId) {
        UUID userId = authUtil.getUserId();

        SpeakingAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (!attempt.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: this attempt does not belong to you");
        }

        SpeakingTopic topic = topicRepository.findById(attempt.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found: " + attempt.getTopicId()));

        return speakingMapper.toAttemptResponse(attempt, topic);
    }

    @Override
    public PageResponse<SpeakingAttemptResponse> getAttemptHistory(int page, int size) {
        UUID userId = authUtil.getUserId();
        List<SpeakingAttempt> allAttempts = attemptRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        int total = allAttempts.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SpeakingAttempt> pageAttempts = allAttempts.subList(fromIndex, toIndex);

        List<UUID> topicIds = pageAttempts.stream()
                .map(SpeakingAttempt::getTopicId)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<UUID, SpeakingTopic> topicMap = topicRepository.findAllById(topicIds).stream()
                .collect(Collectors.toMap(SpeakingTopic::getId, t -> t));

        List<SpeakingAttemptResponse> items = pageAttempts.stream()
                .map(att -> {
                    SpeakingTopic topic = topicMap.get(att.getTopicId());
                    return speakingMapper.toAttemptResponse(att, topic);
                })
                .collect(Collectors.toList());

        return PageResponse.<SpeakingAttemptResponse>builder()
                .page(page)
                .size(size)
                .totalElements((long) total)
                .totalPages((int) Math.ceil((double) total / size))
                .items(items)
                .build();
    }
}
