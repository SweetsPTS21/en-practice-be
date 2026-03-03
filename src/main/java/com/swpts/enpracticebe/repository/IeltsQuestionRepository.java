package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IeltsQuestionRepository extends JpaRepository<IeltsQuestion, UUID> {

    List<IeltsQuestion> findByPassageIdOrderByQuestionOrder(UUID passageId);

    /**
     * Fetch all questions belonging to a test (through test → sections → passages →
     * questions).
     */
    @Query("""
                SELECT q FROM IeltsQuestion q
                JOIN IeltsPassage p ON q.passageId = p.id
                JOIN IeltsSection s ON p.sectionId = s.id
                WHERE s.testId = :testId
                ORDER BY s.sectionOrder, p.passageOrder, q.questionOrder
            """)
    List<IeltsQuestion> findAllByTestId(@Param("testId") UUID testId);
}
