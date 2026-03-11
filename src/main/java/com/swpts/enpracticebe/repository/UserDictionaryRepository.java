package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.UserDictionary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserDictionaryRepository extends JpaRepository<UserDictionary, UUID> {

    boolean existsByUserIdAndWordIgnoreCase(UUID userId, String word);

    @Query(value = """
            SELECT * FROM user_dictionary d
            WHERE d.user_id = :userId
              AND (:keyword IS NULL OR LOWER(d.word) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:wordType IS NULL OR d.word_type = :wordType)
              AND (:isFavorite IS NULL OR d.is_favorite = :isFavorite)
              AND (CAST(:proficiencyLevel AS INTEGER) IS NULL OR d.proficiency_level = CAST(:proficiencyLevel AS INTEGER))
              AND (:tag IS NULL OR d.tags @> CAST(:jsonTag AS jsonb))
            """, nativeQuery = true)
    Page<UserDictionary> searchAdvanced(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("wordType") String wordType,
            @Param("isFavorite") Boolean isFavorite,
            @Param("proficiencyLevel") Integer proficiencyLevel,
            @Param("tag") String tag,
            @Param("jsonTag") String jsonTag, // Need to pass '"tag"' for proper jsonb containment check in postgres
            Pageable pageable
    );

    long countByUserId(UUID userId);

    long countByUserIdAndIsFavoriteTrue(UUID userId);

    long countByUserIdAndProficiencyLevel(UUID userId, Integer proficiencyLevel);

    long countByUserIdAndProficiencyLevelGreaterThanEqual(UUID userId, Integer proficiencyLevel);

    long countByUserIdAndNextReviewAtBefore(UUID userId, java.time.Instant date);

    Page<UserDictionary> findByUserIdAndNextReviewAtBefore(UUID userId, java.time.Instant date, Pageable pageable);
}
