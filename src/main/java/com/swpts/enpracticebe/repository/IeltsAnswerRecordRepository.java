package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsAnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsAnswerRecordRepository extends JpaRepository<IeltsAnswerRecord, UUID> {
    List<IeltsAnswerRecord> findByAttemptId(UUID attemptId);

    @Query(value = """
            SELECT q.question_type as questionType, 
                   COUNT(*) as total, 
                   SUM(CASE WHEN ar.is_correct THEN 1 ELSE 0 END) as correctCount
            FROM ielts_answer_records ar
            JOIN ielts_questions q ON ar.question_id = q.id
            JOIN ielts_test_attempts a ON ar.attempt_id = a.id
            WHERE a.user_id = :userId
            GROUP BY q.question_type
            """, nativeQuery = true)
    List<Object[]> findQuestionTypeAccuracy(@Param("userId") UUID userId);
}
