package com.swpts.enpracticebe.dto.speech;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated speech analytics computed from word-level STT data.
 * Used in:
 * - WebSocket "speech_summary" event (server → client, real-time)
 * - HTTP submit requests (client → server, alongside transcript)
 * - HTTP attempt/turn responses (server → client, historical)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechAnalyticsDto {

    // ─── Basic metrics ────────────────────────────────────────────────────────
    /** Total number of words recognized */
    private Integer wordCount;

    /** Words per minute — IELTS fluent target: 110-140 WPM */
    private Double wordsPerMinute;

    // ─── Pause analysis ───────────────────────────────────────────────────────
    /** Number of pauses exceeding pauseThresholdMs (default 1000ms) */
    private Integer pauseCount;

    /** Average duration (ms) of detected pauses */
    private Double avgPauseDurationMs;

    /** Number of long pauses exceeding 2000ms — strong fluency indicator */
    private Integer longPauseCount;

    // ─── Filler words ─────────────────────────────────────────────────────────
    /** Total count of filler word occurrences (um, uh, like, you know, etc.) */
    private Integer fillerWordCount;

    /** List of detected filler word instances in order of occurrence */
    private List<String> fillerWords;

    // ─── Pronunciation confidence ─────────────────────────────────────────────
    /** Average ASR word confidence score 0.0–1.0 across all words */
    private Double avgWordConfidence;

    /**
     * Words with confidence below threshold (default 0.75).
     * These likely indicate mispronunciation or unclear articulation.
     */
    private List<String> lowConfidenceWords;

    /**
     * Full word-level detail: timing + confidence for each word.
     * Used by frontend to render per-word confidence highlighting.
     */
    private List<SpeechWordDto> wordDetails;
}

