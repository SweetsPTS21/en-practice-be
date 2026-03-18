package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.speech.SpeechAnalyticsDto;
import com.swpts.enpracticebe.dto.speech.SpeechWordDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes speech analytics from word-level STT data.
 * <p>
 * Metrics computed:
 * <ul>
 *   <li>Words per minute (WPM)</li>
 *   <li>Pause count / average pause duration / long pause count</li>
 *   <li>Filler word detection (um, uh, like, you know, etc.)</li>
 *   <li>Average ASR word confidence</li>
 *   <li>Low-confidence words (likely mispronounced)</li>
 * </ul>
 */
@Slf4j
@Service
public class SpeechAnalyticsService {

    /** Words considered as filler/hesitation markers */
    private static final Set<String> FILLER_WORDS = Set.of(
            "um", "uh", "er", "hmm", "like", "well", "so", "actually",
            "basically", "literally", "right", "okay", "you know"
    );

    /** Gap between words (ms) that counts as a noticeable pause */
    @Value("${stt.google.pause-threshold-ms:1000}")
    private int pauseThresholdMs;

    /** Gap between words (ms) that counts as a long/disruptive pause */
    private static final int LONG_PAUSE_THRESHOLD_MS = 2000;

    /** Confidence below this level = potentially mispronounced */
    @Value("${stt.google.low-confidence-threshold:0.75}")
    private float lowConfidenceThreshold;

    /**
     * Analyse a list of recognized words and return aggregated metrics.
     *
     * @param words word-level data from Google STT (must be from final results only)
     * @return analytics result; empty-safe (never null)
     */
    public SpeechAnalyticsDto analyze(List<SpeechWordDto> words) {
        if (words == null || words.isEmpty()) {
            log.debug("SpeechAnalytics: no words to analyse");
            return SpeechAnalyticsDto.builder()
                    .wordCount(0)
                    .wordsPerMinute(0.0)
                    .pauseCount(0)
                    .avgPauseDurationMs(0.0)
                    .longPauseCount(0)
                    .fillerWordCount(0)
                    .fillerWords(List.of())
                    .avgWordConfidence(0.0)
                    .lowConfidenceWords(List.of())
                    .wordDetails(List.of())
                    .build();
        }

        int wordCount = words.size();

        // ─── Speech rate (WPM) ────────────────────────────────────────────────
        long firstStart = words.get(0).getStartMs();
        long lastEnd = words.get(words.size() - 1).getEndMs();
        long durationMs = lastEnd - firstStart;
        double wpm = durationMs > 0 ? wordCount / (durationMs / 60_000.0) : 0.0;

        // ─── Pause detection ──────────────────────────────────────────────────
        int pauseCount = 0;
        int longPauseCount = 0;
        long totalPauseDurationMs = 0;

        for (int i = 1; i < words.size(); i++) {
            long gap = words.get(i).getStartMs() - words.get(i - 1).getEndMs();
            if (gap > pauseThresholdMs) {
                pauseCount++;
                totalPauseDurationMs += gap;
                if (gap > LONG_PAUSE_THRESHOLD_MS) {
                    longPauseCount++;
                }
            }
        }
        double avgPauseDurationMs = pauseCount > 0 ? (double) totalPauseDurationMs / pauseCount : 0.0;

        // ─── Filler words ─────────────────────────────────────────────────────
        List<String> fillerInstances = new ArrayList<>();
        for (SpeechWordDto w : words) {
            String clean = w.getWord().toLowerCase().replaceAll("[^a-z ]", "").trim();
            if (FILLER_WORDS.contains(clean)) {
                fillerInstances.add(w.getWord());
            }
        }

        // ─── Confidence ───────────────────────────────────────────────────────
        double avgConfidence = words.stream()
                .mapToDouble(SpeechWordDto::getConfidence)
                .average()
                .orElse(0.0);

        List<String> lowConfidenceWords = words.stream()
                .filter(w -> w.getConfidence() > 0 && w.getConfidence() < lowConfidenceThreshold)
                .map(SpeechWordDto::getWord)
                .distinct()
                .collect(Collectors.toList());

        log.debug("SpeechAnalytics: words={}, wpm={}, pauses={}, fillers={}, avgConf={}",
                wordCount, round(wpm, 1), pauseCount, fillerInstances.size(), round(avgConfidence, 3));

        return SpeechAnalyticsDto.builder()
                .wordCount(wordCount)
                .wordsPerMinute(round(wpm, 1))
                .pauseCount(pauseCount)
                .avgPauseDurationMs(round(avgPauseDurationMs, 1))
                .longPauseCount(longPauseCount)
                .fillerWordCount(fillerInstances.size())
                .fillerWords(fillerInstances)
                .avgWordConfidence(round(avgConfidence, 4))
                .lowConfidenceWords(lowConfidenceWords)
                .wordDetails(words)
                .build();
    }

    private static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}

