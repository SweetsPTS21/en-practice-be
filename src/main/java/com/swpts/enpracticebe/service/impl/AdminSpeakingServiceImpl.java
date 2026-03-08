package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.admin.CreateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.admin.UpdateSpeakingTopicRequest;
import com.swpts.enpracticebe.dto.request.speaking.SpeakingTopicFilterRequest;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.dto.response.admin.AdminSpeakingTopicResponse;
import com.swpts.enpracticebe.entity.SpeakingTopic;
import com.swpts.enpracticebe.mapper.SpeakingMapper;
import com.swpts.enpracticebe.repository.SpeakingTopicRepository;
import com.swpts.enpracticebe.service.AdminSpeakingService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminSpeakingServiceImpl implements AdminSpeakingService {

    private final SpeakingTopicRepository topicRepository;
    private final SpeakingMapper speakingMapper;

    @Override
    @Cacheable(value = "adminSpeakingTopicList",
            key = "#request.page + '-' + #request.size + '-' + #request.part + '-' + #request.difficulty + '-' + #request.isPublished")
    public PageResponse<AdminSpeakingTopicResponse> listTopics(SpeakingTopicFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasPart = request.getPart() != null && !request.getPart().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();
        boolean hasPublished = request.getIsPublished() != null;

        Page<SpeakingTopic> page = findTopics(hasPart, hasDifficulty, hasPublished, request, pageable);

        List<AdminSpeakingTopicResponse> items = page.getContent().stream()
                .map(speakingMapper::toAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminSpeakingTopicResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    @Cacheable(value = "speakingTopicDetail", key = "#topicId")
    public AdminSpeakingTopicResponse getTopicDetail(UUID topicId) {
        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Speaking topic not found: " + topicId));
        return speakingMapper.toAdminResponse(topic);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminSpeakingTopicList", allEntries = true),
            @CacheEvict(value = "speakingTopicList", allEntries = true)
    })
    public AdminSpeakingTopicResponse createTopic(CreateSpeakingTopicRequest request) {
        SpeakingTopic topic = SpeakingTopic.builder()
                .part(SpeakingTopic.Part.valueOf(request.getPart()))
                .question(request.getQuestion())
                .cueCard(request.getCueCard())
                .followUpQuestions(request.getFollowUpQuestions())
                .aiGradingPrompt(request.getAiGradingPrompt())
                .difficulty(SpeakingTopic.Difficulty.valueOf(request.getDifficulty()))
                .isPublished(request.getIsPublished())
                .build();
        topic = topicRepository.save(topic);
        return speakingMapper.toAdminResponse(topic);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "speakingTopicDetail", key = "#topicId"),
            @CacheEvict(value = "adminSpeakingTopicList", allEntries = true),
            @CacheEvict(value = "speakingTopicList", allEntries = true)
    })
    public AdminSpeakingTopicResponse updateTopic(UUID topicId, UpdateSpeakingTopicRequest request) {
        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Speaking topic not found: " + topicId));

        topic.setPart(SpeakingTopic.Part.valueOf(request.getPart()));
        topic.setQuestion(request.getQuestion());
        topic.setCueCard(request.getCueCard());
        topic.setFollowUpQuestions(request.getFollowUpQuestions());
        topic.setAiGradingPrompt(request.getAiGradingPrompt());
        topic.setDifficulty(SpeakingTopic.Difficulty.valueOf(request.getDifficulty()));
        topic.setIsPublished(request.getIsPublished());
        topic = topicRepository.save(topic);

        return speakingMapper.toAdminResponse(topic);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "speakingTopicDetail", key = "#topicId"),
            @CacheEvict(value = "adminSpeakingTopicList", allEntries = true),
            @CacheEvict(value = "speakingTopicList", allEntries = true)
    })
    public void deleteTopic(UUID topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new RuntimeException("Speaking topic not found: " + topicId);
        }
        topicRepository.deleteById(topicId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "speakingTopicDetail", key = "#topicId"),
            @CacheEvict(value = "adminSpeakingTopicList", allEntries = true),
            @CacheEvict(value = "speakingTopicList", allEntries = true)
    })
    public void togglePublish(UUID topicId, boolean published) {
        SpeakingTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Speaking topic not found: " + topicId));
        topic.setIsPublished(published);
        topicRepository.save(topic);
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private Page<SpeakingTopic> findTopics(boolean hasPart, boolean hasDifficulty, boolean hasPublished,
                                           SpeakingTopicFilterRequest request, PageRequest pageable) {
        SpeakingTopic.Part part = hasPart ? SpeakingTopic.Part.valueOf(request.getPart()) : null;
        SpeakingTopic.Difficulty difficulty = hasDifficulty
                ? SpeakingTopic.Difficulty.valueOf(request.getDifficulty()) : null;

        if (hasPart && hasDifficulty && hasPublished) {
            return topicRepository.findByPartAndDifficultyAndIsPublished(part, difficulty, request.getIsPublished(), pageable);
        } else if (hasPart && hasDifficulty) {
            return topicRepository.findByPartAndDifficulty(part, difficulty, pageable);
        } else if (hasPart && hasPublished) {
            return topicRepository.findByPartAndIsPublished(part, request.getIsPublished(), pageable);
        } else if (hasDifficulty && hasPublished) {
            return topicRepository.findByDifficultyAndIsPublished(difficulty, request.getIsPublished(), pageable);
        } else if (hasPart) {
            return topicRepository.findByPart(part, pageable);
        } else if (hasDifficulty) {
            return topicRepository.findByDifficulty(difficulty, pageable);
        } else if (hasPublished) {
            return topicRepository.findByIsPublished(request.getIsPublished(), pageable);
        } else {
            return topicRepository.findAll(pageable);
        }
    }
}
