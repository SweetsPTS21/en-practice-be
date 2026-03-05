package com.swpts.enpracticebe.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ielts_questions", uniqueConstraints = @UniqueConstraint(columnNames = {"passage_id",
        "question_order"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IeltsQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "passage_id", nullable = false)
    private UUID passageId;
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> options = new ArrayList<>();
    @Type(JsonBinaryType.class)
    @Column(name = "correct_answers", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private List<String> correctAnswers = new ArrayList<>();
    @Column(columnDefinition = "TEXT")
    private String explanation;

    public enum QuestionType {
        // Listening
        FORM_COMPLETION,
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        MAP_LABELING,
        SENTENCE_COMPLETION,
        MATCHING,
        // Reading
        TRUE_FALSE_NOT_GIVEN,
        MATCHING_HEADINGS,
        SUMMARY_COMPLETION,
        YES_NO_NOT_GIVEN
    }
}
