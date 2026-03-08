package com.swpts.enpracticebe.mapper;

import com.swpts.enpracticebe.dto.response.listening.IeltsTestDetailResponse;
import com.swpts.enpracticebe.dto.response.listening.IeltsTestListResponse;
import com.swpts.enpracticebe.entity.IeltsPassage;
import com.swpts.enpracticebe.entity.IeltsQuestion;
import com.swpts.enpracticebe.entity.IeltsSection;
import com.swpts.enpracticebe.entity.IeltsTest;
import com.swpts.enpracticebe.repository.IeltsPassageRepository;
import com.swpts.enpracticebe.repository.IeltsQuestionRepository;
import com.swpts.enpracticebe.repository.IeltsSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IeltsMapper {
    private final IeltsQuestionRepository questionRepository;
    private final IeltsSectionRepository sectionRepository;
    private final IeltsPassageRepository passageRepository;

    public IeltsTestListResponse toListResponse(IeltsTest test) {
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

    public IeltsTestDetailResponse buildTestDetail(IeltsTest test) {
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

    public IeltsTestDetailResponse buildDetailResponse(IeltsTest test) {
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
