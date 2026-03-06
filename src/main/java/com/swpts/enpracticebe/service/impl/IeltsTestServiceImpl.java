package com.swpts.enpracticebe.service.impl;

import com.swpts.enpracticebe.dto.request.AnswerItem;
import com.swpts.enpracticebe.dto.request.IeltsTestFilterRequest;
import com.swpts.enpracticebe.dto.request.SubmitTestRequest;
import com.swpts.enpracticebe.dto.response.*;
import com.swpts.enpracticebe.entity.*;
import com.swpts.enpracticebe.repository.*;
import com.swpts.enpracticebe.service.IeltsTestService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IeltsTestServiceImpl implements IeltsTestService {

    private final IeltsTestRepository testRepository;
    private final IeltsSectionRepository sectionRepository;
    private final IeltsPassageRepository passageRepository;
    private final IeltsQuestionRepository questionRepository;
    private final IeltsTestAttemptRepository attemptRepository;
    private final IeltsAnswerRecordRepository answerRecordRepository;

    public IeltsTestServiceImpl(IeltsTestRepository testRepository,
            IeltsSectionRepository sectionRepository,
            IeltsPassageRepository passageRepository,
            IeltsQuestionRepository questionRepository,
            IeltsTestAttemptRepository attemptRepository,
            IeltsAnswerRecordRepository answerRecordRepository) {
        this.testRepository = testRepository;
        this.sectionRepository = sectionRepository;
        this.passageRepository = passageRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRecordRepository = answerRecordRepository;
    }

    // ─── Band score mapping (IELTS standard for Listening/Reading out of 40) ───
    private static final int[][] BAND_TABLE = {
            { 39, 90 }, // 39-40 → 9.0
            { 37, 85 }, // 37-38 → 8.5
            { 35, 80 }, // 35-36 → 8.0
            { 33, 75 }, // 33-34 → 7.5
            { 30, 70 }, // 30-32 → 7.0
            { 27, 65 }, // 27-29 → 6.5
            { 23, 60 }, // 23-26 → 6.0
            { 20, 55 }, // 20-22 → 5.5
            { 16, 50 }, // 16-19 → 5.0
            { 13, 45 }, // 13-15 → 4.5
            { 10, 40 }, // 10-12 → 4.0
            { 6, 35 }, // 6-9 → 3.5
            { 4, 30 }, // 4-5 → 3.0
            { 0, 25 }, // 0-3 → 2.5
    };

    // ─── Public API ─────────────────────────────────────────────────────────────

    @Override
    @Cacheable(value = "ieltsTestList", key = "#request.page + '-' + #request.size + '-' + #request.skill + '-' + #request.difficulty")
    public PageResponse<IeltsTestListResponse> getTests(IeltsTestFilterRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<IeltsTest> page;

        boolean hasSkill = request.getSkill() != null && !request.getSkill().isBlank();
        boolean hasDifficulty = request.getDifficulty() != null && !request.getDifficulty().isBlank();

        if (hasSkill && hasDifficulty) {
            page = testRepository.findBySkillAndDifficultyAndIsPublishedTrue(
                    IeltsTest.Skill.valueOf(request.getSkill()),
                    IeltsTest.Difficulty.valueOf(request.getDifficulty()),
                    pageable);
        } else if (hasSkill) {
            page = testRepository.findBySkillAndIsPublishedTrue(
                    IeltsTest.Skill.valueOf(request.getSkill()), pageable);
        } else if (hasDifficulty) {
            page = testRepository.findByDifficultyAndIsPublishedTrue(
                    IeltsTest.Difficulty.valueOf(request.getDifficulty()), pageable);
        } else {
            page = testRepository.findByIsPublishedTrue(pageable);
        }

        List<IeltsTestListResponse> items = page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.<IeltsTestListResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    @Cacheable(value = "ieltsTestDetail", key = "#testId")
    public IeltsTestDetailResponse getTestDetail(UUID testId) {
        IeltsTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));
        return buildTestDetail(test);
    }

    @Override
    @Transactional
    public StartTestResponse startTest(UUID testId) {
        UUID userId = getCurrentUserId();
        IeltsTest test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found: " + testId));

        List<IeltsQuestion> allQuestions = questionRepository.findAllByTestId(testId);

        IeltsTestAttempt attempt = IeltsTestAttempt.builder()
                .userId(userId)
                .testId(testId)
                .totalQuestions(allQuestions.size())
                .status(IeltsTestAttempt.AttemptStatus.IN_PROGRESS)
                .build();
        attemptRepository.save(attempt);

        return StartTestResponse.builder()
                .attemptId(attempt.getId())
                .testDetail(buildTestDetail(test))
                .build();
    }

    @Override
    @Transactional
    public SubmitTestResponse submitTest(SubmitTestRequest request) {
        UUID userId = getCurrentUserId();

        IeltsTestAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + request.getAttemptId()));

        if (!attempt.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: this attempt does not belong to you");
        }
        if (attempt.getStatus() == IeltsTestAttempt.AttemptStatus.COMPLETED) {
            throw new RuntimeException("This attempt has already been submitted");
        }

        // Load all questions for this test
        List<IeltsQuestion> allQuestions = questionRepository.findAllByTestId(attempt.getTestId());

        // Build user answer map
        Map<UUID, List<String>> userAnswerMap = new HashMap<>();
        if (request.getAnswers() != null) {
            for (AnswerItem item : request.getAnswers()) {
                userAnswerMap.put(item.getQuestionId(), item.getUserAnswer());
            }
        }

        // Grade each question
        int correctCount = 0;
        List<SubmitTestResponse.AnswerResultItem> results = new ArrayList<>();
        List<IeltsAnswerRecord> records = new ArrayList<>();

        for (IeltsQuestion question : allQuestions) {
            List<String> userAnswer = userAnswerMap.getOrDefault(question.getId(), Collections.emptyList());
            boolean isCorrect = checkAnswer(question.getCorrectAnswers(), userAnswer);

            if (isCorrect)
                correctCount++;

            records.add(IeltsAnswerRecord.builder()
                    .attemptId(attempt.getId())
                    .questionId(question.getId())
                    .userAnswer(userAnswer)
                    .isCorrect(isCorrect)
                    .build());

            results.add(SubmitTestResponse.AnswerResultItem.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .questionType(question.getQuestionType().name())
                    .userAnswer(userAnswer)
                    .correctAnswer(question.getCorrectAnswers())
                    .isCorrect(isCorrect)
                    .explanation(question.getExplanation())
                    .build());
        }

        // Save answer records
        answerRecordRepository.saveAll(records);

        // Calculate band score
        float bandScore = calculateBandScore(correctCount, allQuestions.size());

        // Update attempt
        attempt.setCorrectCount(correctCount);
        attempt.setBandScore(bandScore);
        attempt.setTimeSpentSeconds(request.getTimeSpentSeconds());
        attempt.setStatus(IeltsTestAttempt.AttemptStatus.COMPLETED);
        attempt.setCompletedAt(Instant.now());
        attemptRepository.save(attempt);

        return SubmitTestResponse.builder()
                .attemptId(attempt.getId())
                .totalQuestions(allQuestions.size())
                .correctCount(correctCount)
                .bandScore(bandScore)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .results(results)
                .build();
    }

    @Override
    public PageResponse<TestAttemptHistoryResponse> getAttemptHistory(int page, int size) {
        UUID userId = getCurrentUserId();
        List<IeltsTestAttempt> allAttempts = attemptRepository.findByUserIdOrderByStartedAtDesc(userId);

        // Manual pagination
        int total = allAttempts.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<IeltsTestAttempt> pageAttempts = allAttempts.subList(fromIndex, toIndex);

        // Map test titles
        Set<UUID> testIds = pageAttempts.stream()
                .map(IeltsTestAttempt::getTestId)
                .collect(Collectors.toSet());
        Map<UUID, IeltsTest> testMap = testRepository.findAllById(testIds).stream()
                .collect(Collectors.toMap(IeltsTest::getId, t -> t));

        List<TestAttemptHistoryResponse> items = pageAttempts.stream()
                .map(a -> {
                    IeltsTest test = testMap.get(a.getTestId());
                    return TestAttemptHistoryResponse.builder()
                            .attemptId(a.getId())
                            .testId(a.getTestId())
                            .testTitle(test != null ? test.getTitle() : "Unknown")
                            .skill(test != null ? test.getSkill().name() : null)
                            .totalQuestions(a.getTotalQuestions())
                            .correctCount(a.getCorrectCount())
                            .bandScore(a.getBandScore())
                            .timeSpentSeconds(a.getTimeSpentSeconds())
                            .status(a.getStatus().name())
                            .startedAt(a.getStartedAt())
                            .completedAt(a.getCompletedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponse.<TestAttemptHistoryResponse>builder()
                .page(page)
                .size(size)
                .totalElements((long) total)
                .totalPages((int) Math.ceil((double) total / size))
                .items(items)
                .build();
    }

    @Override
    public SubmitTestResponse getAttemptDetail(UUID attemptId) {
        UUID userId = getCurrentUserId();

        IeltsTestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (!attempt.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: this attempt does not belong to you");
        }

        List<IeltsAnswerRecord> records = answerRecordRepository.findByAttemptId(attemptId);
        Map<UUID, IeltsAnswerRecord> recordMap = records.stream()
                .collect(Collectors.toMap(IeltsAnswerRecord::getQuestionId, r -> r));

        List<IeltsQuestion> allQuestions = questionRepository.findAllByTestId(attempt.getTestId());

        List<SubmitTestResponse.AnswerResultItem> results = allQuestions.stream()
                .map(q -> {
                    IeltsAnswerRecord rec = recordMap.get(q.getId());
                    return SubmitTestResponse.AnswerResultItem.builder()
                            .questionId(q.getId())
                            .questionText(q.getQuestionText())
                            .questionType(q.getQuestionType().name())
                            .userAnswer(rec != null ? rec.getUserAnswer() : Collections.emptyList())
                            .correctAnswer(q.getCorrectAnswers())
                            .isCorrect(rec != null && rec.getIsCorrect())
                            .explanation(q.getExplanation())
                            .build();
                })
                .collect(Collectors.toList());

        return SubmitTestResponse.builder()
                .attemptId(attempt.getId())
                .totalQuestions(attempt.getTotalQuestions())
                .correctCount(attempt.getCorrectCount())
                .bandScore(attempt.getBandScore())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .results(results)
                .build();
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private IeltsTestListResponse toListResponse(IeltsTest test) {
        int totalQ = questionRepository.findAllByTestId(test.getId()).size();
        return IeltsTestListResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .skill(test.getSkill().name())
                .timeLimitMinutes(test.getTimeLimitMinutes())
                .difficulty(test.getDifficulty().name())
                .totalQuestions(totalQ)
                .createdAt(test.getCreatedAt())
                .build();
    }

    private IeltsTestDetailResponse buildTestDetail(IeltsTest test) {
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
                .sections(sectionDtos)
                .build();
    }

    /**
     * Compare user answers against correct answers (case-insensitive, trimmed).
     * Both are List<String> — answers match if all elements match in order.
     */
    private boolean checkAnswer(List<String> correctAnswers, List<String> userAnswers) {
        if (correctAnswers == null || userAnswers == null)
            return false;
        if (correctAnswers.size() != userAnswers.size())
            return false;

        for (int i = 0; i < correctAnswers.size(); i++) {
            String correct = correctAnswers.get(i).trim().toLowerCase();
            String user = userAnswers.get(i).trim().toLowerCase();
            if (!correct.equals(user))
                return false;
        }
        return true;
    }

    /**
     * Calculate IELTS band score based on correct count.
     * Scales proportionally if totalQuestions != 40.
     */
    private float calculateBandScore(int correctCount, int totalQuestions) {
        // Normalize to out-of-40 scale
        int normalized = totalQuestions > 0
                ? Math.round((float) correctCount / totalQuestions * 40)
                : 0;

        for (int[] entry : BAND_TABLE) {
            if (normalized >= entry[0]) {
                return entry[1] / 10.0f;
            }
        }
        return 2.0f;
    }
}
