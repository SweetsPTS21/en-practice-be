package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.AdminIeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.CreateIeltsTestRequest;
import com.swpts.enpracticebe.dto.request.UpdateIeltsTestRequest;
import com.swpts.enpracticebe.dto.response.IeltsTestDetailResponse;
import com.swpts.enpracticebe.dto.response.AdminIeltsTestListResponse;
import com.swpts.enpracticebe.dto.response.PageResponse;
import com.swpts.enpracticebe.entity.IeltsPassage;
import com.swpts.enpracticebe.entity.IeltsQuestion;
import com.swpts.enpracticebe.entity.IeltsSection;
import com.swpts.enpracticebe.entity.IeltsTest;
import com.swpts.enpracticebe.repository.IeltsPassageRepository;
import com.swpts.enpracticebe.repository.IeltsQuestionRepository;
import com.swpts.enpracticebe.repository.IeltsSectionRepository;
import com.swpts.enpracticebe.repository.IeltsTestRepository;
import com.swpts.enpracticebe.service.AdminIeltsTestService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminIeltsTestServiceImpl implements AdminIeltsTestService {

    private final IeltsTestRepository testRepository;
    private final IeltsSectionRepository sectionRepository;
    private final IeltsPassageRepository passageRepository;
    private final IeltsQuestionRepository questionRepository;

    public AdminIeltsTestServiceImpl(IeltsTestRepository testRepository,
            IeltsSectionRepository sectionRepository,
            IeltsPassageRepository passageRepository,
            IeltsQuestionRepository questionRepository) {
        this.testRepository = testRepository;
        this.sectionRepository = sectionRepository;
        this.passageRepository = passageRepository;
        this.questionRepository = questionRepository;
    }

    // ─── List Tests ──────────────────────────────────────────────────────────────

    @Override
    @Cacheable(value = "adminIeltsTestList", key = "#request.page + '-' + #request.size + '-' + #request.skill + '-' + #request.difficulty + '-' + #request.isPublished")
    public PageResponse<AdminIeltsTestListResponse> listTests(AdminIeltsTestFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasSkill = request.getSkill() != null && !request.getSkill().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();
        boolean hasPublished = request.getIsPublished() != null;

        Page<IeltsTest> page = findTests(hasSkill, hasDifficulty, hasPublished, request, pageable);

        List<AdminIeltsTestListResponse> items = page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminIeltsTestListResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    // ─── Get Test Detail ─────────────────────────────────────────────────────────

    @Override
    @Cacheable(value = "ieltsTestDetail", key = "#testId")
    public IeltsTestDetailResponse getTestDetail(UUID testId) {
        IeltsTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));
        return buildDetailResponse(test);
    }

    // ─── Create Test ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminIeltsTestList", allEntries = true),
            @CacheEvict(value = "ieltsTestList", allEntries = true)
    })
    public IeltsTestDetailResponse createTest(CreateIeltsTestRequest request) {
        IeltsTest test = IeltsTest.builder()
                .title(request.getTitle())
                .skill(IeltsTest.Skill.valueOf(request.getSkill()))
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .difficulty(IeltsTest.Difficulty.valueOf(request.getDifficulty()))
                .isPublished(request.getIsPublished())
                .build();
        test = testRepository.save(test);

        saveNestedStructure(test.getId(), request.getSections());

        return buildDetailResponse(test);
    }

    // ─── Update Test ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ieltsTestDetail", key = "#testId"),
            @CacheEvict(value = "adminIeltsTestList", allEntries = true),
            @CacheEvict(value = "ieltsTestList", allEntries = true)
    })
    public IeltsTestDetailResponse updateTest(UUID testId, UpdateIeltsTestRequest request) {
        IeltsTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));

        // Update test metadata
        test.setTitle(request.getTitle());
        test.setSkill(IeltsTest.Skill.valueOf(request.getSkill()));
        test.setTimeLimitMinutes(request.getTimeLimitMinutes());
        test.setDifficulty(IeltsTest.Difficulty.valueOf(request.getDifficulty()));
        test.setIsPublished(request.getIsPublished());
        testRepository.save(test);

        // Delete old nested data
        deleteNestedData(testId);

        // Re-create nested structure
        saveNestedStructure(testId, request.getSections());

        return buildDetailResponse(test);
    }

    // ─── Delete Test ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ieltsTestDetail", key = "#testId"),
            @CacheEvict(value = "adminIeltsTestList", allEntries = true),
            @CacheEvict(value = "ieltsTestList", allEntries = true)
    })
    public void deleteTest(UUID testId) {
        if (!testRepository.existsById(testId)) {
            throw new RuntimeException("Test not found: " + testId);
        }
        // DB foreign keys have ON DELETE CASCADE, so just delete the test
        testRepository.deleteById(testId);
    }

    // ─── Toggle Publish ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ieltsTestDetail", key = "#testId"),
            @CacheEvict(value = "adminIeltsTestList", allEntries = true),
            @CacheEvict(value = "ieltsTestList", allEntries = true)
    })
    public void togglePublish(UUID testId, boolean published) {
        IeltsTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));
        test.setIsPublished(published);
        testRepository.save(test);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    private void saveNestedStructure(UUID testId, List<CreateIeltsTestRequest.SectionRequest> sectionRequests) {
        if (sectionRequests == null)
            return;

        for (CreateIeltsTestRequest.SectionRequest sReq : sectionRequests) {
            IeltsSection section = IeltsSection.builder()
                    .testId(testId)
                    .sectionOrder(sReq.getSectionOrder())
                    .title(sReq.getTitle())
                    .audioUrl(sReq.getAudioUrl())
                    .instructions(sReq.getInstructions())
                    .build();
            section = sectionRepository.save(section);

            if (sReq.getPassages() == null)
                continue;

            for (CreateIeltsTestRequest.PassageRequest pReq : sReq.getPassages()) {
                IeltsPassage passage = IeltsPassage.builder()
                        .sectionId(section.getId())
                        .passageOrder(pReq.getPassageOrder())
                        .title(pReq.getTitle())
                        .content(pReq.getContent())
                        .build();
                passage = passageRepository.save(passage);

                if (pReq.getQuestions() == null)
                    continue;

                List<IeltsQuestion> questions = new ArrayList<>();
                for (CreateIeltsTestRequest.QuestionRequest qReq : pReq.getQuestions()) {
                    questions.add(IeltsQuestion.builder()
                            .passageId(passage.getId())
                            .questionOrder(qReq.getQuestionOrder())
                            .questionType(IeltsQuestion.QuestionType.valueOf(qReq.getQuestionType()))
                            .questionText(qReq.getQuestionText())
                            .options(qReq.getOptions() != null ? qReq.getOptions() : new ArrayList<>())
                            .correctAnswers(qReq.getCorrectAnswers())
                            .explanation(qReq.getExplanation())
                            .build());
                }
                questionRepository.saveAll(questions);
            }
        }
    }

    private void deleteNestedData(UUID testId) {
        List<IeltsSection> sections = sectionRepository.findByTestIdOrderBySectionOrder(testId);
        if (sections.isEmpty())
            return;

        List<UUID> sectionIds = sections.stream().map(IeltsSection::getId).collect(Collectors.toList());
        List<IeltsPassage> passages = passageRepository.findBySectionIdIn(sectionIds);

        if (!passages.isEmpty()) {
            List<UUID> passageIds = passages.stream().map(IeltsPassage::getId).collect(Collectors.toList());
            questionRepository.deleteByPassageIdIn(passageIds);
            questionRepository.flush();
            passageRepository.deleteBySectionIdIn(sectionIds);
            passageRepository.flush();
        }

        sectionRepository.deleteByTestId(testId);
        sectionRepository.flush();
    }

    private Page<IeltsTest> findTests(boolean hasSkill, boolean hasDifficulty, boolean hasPublished,
            AdminIeltsTestFilterRequest request, PageRequest pageable) {
        IeltsTest.Skill skill = hasSkill ? IeltsTest.Skill.valueOf(request.getSkill()) : null;
        IeltsTest.Difficulty difficulty = hasDifficulty ? IeltsTest.Difficulty.valueOf(request.getDifficulty()) : null;

        if (hasSkill && hasDifficulty && hasPublished) {
            return testRepository.findBySkillAndDifficultyAndIsPublished(skill, difficulty, request.getIsPublished(),
                    pageable);
        } else if (hasSkill && hasDifficulty) {
            return testRepository.findBySkillAndDifficulty(skill, difficulty, pageable);
        } else if (hasSkill && hasPublished) {
            return testRepository.findBySkillAndIsPublished(skill, request.getIsPublished(), pageable);
        } else if (hasDifficulty && hasPublished) {
            return testRepository.findByDifficultyAndIsPublished(difficulty, request.getIsPublished(), pageable);
        } else if (hasSkill) {
            return testRepository.findBySkill(skill, pageable);
        } else if (hasDifficulty) {
            return testRepository.findByDifficulty(difficulty, pageable);
        } else if (hasPublished) {
            return testRepository.findByIsPublished(request.getIsPublished(), pageable);
        } else {
            return testRepository.findAll(pageable);
        }
    }

    private AdminIeltsTestListResponse toListResponse(IeltsTest test) {
        int totalQ = questionRepository.findAllByTestId(test.getId()).size();
        return AdminIeltsTestListResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .skill(test.getSkill().name())
                .timeLimitMinutes(test.getTimeLimitMinutes())
                .difficulty(test.getDifficulty().name())
                .isPublished(test.getIsPublished())
                .totalQuestions(totalQ)
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
    }

    private IeltsTestDetailResponse buildDetailResponse(IeltsTest test) {
        List<IeltsSection> sections = sectionRepository.findByTestIdOrderBySectionOrder(test.getId());

        List<IeltsTestDetailResponse.SectionDto> sectionDtos = sections.stream()
                .map(section -> {
                    List<IeltsPassage> passages = passageRepository
                            .findBySectionIdOrderByPassageOrder(section.getId());

                    List<IeltsTestDetailResponse.PassageDto> passageDtos = passages.stream()
                            .map(passage -> {
                                List<IeltsQuestion> questions = questionRepository
                                        .findByPassageIdOrderByQuestionOrder(passage.getId());

                                List<IeltsTestDetailResponse.QuestionDto> questionDtos = questions.stream()
                                        .map(q -> IeltsTestDetailResponse.QuestionDto.builder()
                                                .id(q.getId())
                                                .questionOrder(q.getQuestionOrder())
                                                .questionType(q.getQuestionType().name())
                                                .questionText(q.getQuestionText())
                                                .options(q.getOptions())
                                                .correctAnswers(q.getCorrectAnswers())
                                                .explanation(q.getExplanation())
                                                .build())
                                        .collect(Collectors.toList());

                                return IeltsTestDetailResponse.PassageDto.builder()
                                        .id(passage.getId())
                                        .passageOrder(passage.getPassageOrder())
                                        .title(passage.getTitle())
                                        .content(passage.getContent())
                                        .questions(questionDtos)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return IeltsTestDetailResponse.SectionDto.builder()
                            .id(section.getId())
                            .sectionOrder(section.getSectionOrder())
                            .title(section.getTitle())
                            .audioUrl(section.getAudioUrl())
                            .instructions(section.getInstructions())
                            .passages(passageDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return IeltsTestDetailResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .skill(test.getSkill().name())
                .timeLimitMinutes(test.getTimeLimitMinutes())
                .difficulty(test.getDifficulty().name())
                .isPublished(test.getIsPublished())
                .sections(sectionDtos)
                .build();
    }
}
