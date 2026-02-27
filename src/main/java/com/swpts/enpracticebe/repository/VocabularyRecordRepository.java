package com.swpts.enpracticebe.repository;

import com.swpts.enpracticebe.entity.VocabularyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface VocabularyRecordRepository extends JpaRepository<VocabularyRecord, UUID> {

    @Query("""
                SELECT v FROM VocabularyRecord v
                WHERE v.userId = :userId
                  AND (COALESCE(:englishWord, '') = '' OR v.englishWord LIKE %:englishWord%)
                  AND (:isCorrect IS NULL OR v.isCorrect = :isCorrect)
                  AND (v.testedAt >= COALESCE(:from, v.testedAt))
                  AND (v.testedAt <= COALESCE(:to, v.testedAt))
            """)
    Page<VocabularyRecord> searchRecord(
            @Param("userId") UUID userId,
            @Param("englishWord") String englishWord,
            @Param("isCorrect") Boolean isCorrect,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    List<VocabularyRecord> findByUserIdOrderByTestedAtDesc(UUID userId);

    List<VocabularyRecord> findByUserIdAndTestedAtGreaterThanEqual(UUID userId, Instant since);

    List<VocabularyRecord> findByUserIdAndIsCorrectFalse(UUID userId);

    void deleteAllByUserId(UUID userId);

    // Stats: count total, correct, incorrect in a time range
    @Query("SELECT COUNT(v) FROM VocabularyRecord v WHERE v.userId = :userId AND v.testedAt >= :since")
    long countByUserIdAndTestedAtAfter(@Param("userId") UUID userId, @Param("since") Instant since);

    @Query("SELECT COUNT(v) FROM VocabularyRecord v WHERE v.userId = :userId AND v.testedAt >= :since AND v.isCorrect = true")
    long countCorrectByUserIdAndTestedAtAfter(@Param("userId") UUID userId, @Param("since") Instant since);

    // Frequently wrong words
    @Query(value = """
            SELECT v.english_word AS word, v.correct_meaning AS correctMeaning,
                   COUNT(*) AS wrongCount, MAX(v.tested_at) AS lastAttempt
            FROM vocabulary_records v
            WHERE v.user_id = :userId AND v.is_correct = false
            GROUP BY v.english_word, v.correct_meaning
            HAVING COUNT(*) > 0
            ORDER BY COUNT(*) DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findFrequentlyWrongWords(@Param("userId") UUID userId);

    // Streak: get distinct dates with records (ordered desc)
    @Query(value = """
            SELECT DISTINCT DATE(v.tested_at AT TIME ZONE 'UTC') AS record_date
            FROM vocabulary_records v
            WHERE v.user_id = :userId
            ORDER BY record_date DESC
            """, nativeQuery = true)
    List<java.sql.Date> findDistinctRecordDates(@Param("userId") UUID userId);

    // Review words: unique by english_word, latest record for each
    @Query(value = """
            SELECT DISTINCT ON (v.english_word)
                   v.english_word, v.correct_meaning, v.alternatives
            FROM vocabulary_records v
            WHERE v.user_id = :userId AND v.tested_at >= :since
            ORDER BY v.english_word, v.tested_at DESC
            """, nativeQuery = true)
    List<Object[]> findUniqueWordsSince(@Param("userId") UUID userId, @Param("since") Instant since);

    // Review words: wrong only
    @Query(value = """
            SELECT DISTINCT ON (v.english_word)
                   v.english_word, v.correct_meaning, v.alternatives
            FROM vocabulary_records v
            WHERE v.user_id = :userId AND v.is_correct = false
            ORDER BY v.english_word, v.tested_at DESC
            """, nativeQuery = true)
    List<Object[]> findUniqueWrongWords(@Param("userId") UUID userId);

    // Review words: all time
    @Query(value = """
            SELECT DISTINCT ON (v.english_word)
                   v.english_word, v.correct_meaning, v.alternatives
            FROM vocabulary_records v
            WHERE v.user_id = :userId
            ORDER BY v.english_word, v.tested_at DESC
            """, nativeQuery = true)
    List<Object[]> findAllUniqueWords(@Param("userId") UUID userId);

    // Count unique words in time range
    @Query(value = """
            SELECT COUNT(DISTINCT v.english_word)
            FROM vocabulary_records v
            WHERE v.user_id = :userId AND v.tested_at >= :since
            """, nativeQuery = true)
    long countUniqueWordsSince(@Param("userId") UUID userId, @Param("since") Instant since);

    // Count unique wrong words
    @Query(value = """
            SELECT COUNT(DISTINCT v.english_word)
            FROM vocabulary_records v
            WHERE v.user_id = :userId AND v.is_correct = false
            """, nativeQuery = true)
    long countUniqueWrongWords(@Param("userId") UUID userId);

    // Count all unique words
    @Query(value = "SELECT COUNT(DISTINCT v.english_word) FROM vocabulary_records v WHERE v.user_id = :userId", nativeQuery = true)
    long countAllUniqueWords(@Param("userId") UUID userId);

    // Find incorrect records within date range
    @Query("SELECT v FROM VocabularyRecord v WHERE v.testedAt BETWEEN :start AND :end AND v.isCorrect = false")
    List<VocabularyRecord> findByTestedAtBetweenAndIsCorrectFalse(@Param("start") Instant start, @Param("end") Instant end);
}
