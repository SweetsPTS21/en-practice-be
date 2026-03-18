package com.swpts.enpracticebe.dto.speech;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Word-level data captured from Google STT streaming response.
 * Contains timing offsets (ms) and ASR confidence score per word.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeechWordDto {
    /** The recognized word text */
    private String word;
    /** Milliseconds from start of audio to beginning of this word */
    private long startMs;
    /** Milliseconds from start of audio to end of this word */
    private long endMs;
    /**
     * ASR confidence score 0.0–1.0.
     * Low confidence may indicate unclear articulation / mispronunciation.
     * 0.0 means confidence was not available.
     */
    private float confidence;
}

