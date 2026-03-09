package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.IeltsTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IeltsTestRepository extends JpaRepository<IeltsTest, UUID> {

    // User-facing (published only)
    Page<IeltsTest> findByIsPublishedTrue(Pageable pageable);

    Page<IeltsTest> findBySkillAndIsPublishedTrue(IeltsTest.Skill skill, Pageable pageable);

    Page<IeltsTest> findByDifficultyAndIsPublishedTrue(IeltsTest.Difficulty difficulty, Pageable pageable);

    Page<IeltsTest> findBySkillAndDifficultyAndIsPublishedTrue(
            IeltsTest.Skill skill, IeltsTest.Difficulty difficulty, Pageable pageable);

    // Admin (all tests)
    Page<IeltsTest> findBySkill(IeltsTest.Skill skill, Pageable pageable);

    Page<IeltsTest> findByDifficulty(IeltsTest.Difficulty difficulty, Pageable pageable);

    Page<IeltsTest> findBySkillAndDifficulty(IeltsTest.Skill skill, IeltsTest.Difficulty difficulty, Pageable pageable);

    Page<IeltsTest> findByIsPublished(Boolean isPublished, Pageable pageable);

    Page<IeltsTest> findBySkillAndIsPublished(IeltsTest.Skill skill, Boolean isPublished, Pageable pageable);

    Page<IeltsTest> findByDifficultyAndIsPublished(IeltsTest.Difficulty difficulty, Boolean isPublished,
            Pageable pageable);

    Page<IeltsTest> findBySkillAndDifficultyAndIsPublished(
            IeltsTest.Skill skill, IeltsTest.Difficulty difficulty, Boolean isPublished, Pageable pageable);

    long countByIsPublishedTrue();
}
