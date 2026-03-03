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
@Table(name = "ielts_answer_records", uniqueConstraints = @UniqueConstraint(columnNames = { "attempt_id",
        "question_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IeltsAnswerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Type(JsonBinaryType.class)
    @Column(name = "user_answer", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> userAnswer = new ArrayList<>();

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private Boolean isCorrect = false;
}
