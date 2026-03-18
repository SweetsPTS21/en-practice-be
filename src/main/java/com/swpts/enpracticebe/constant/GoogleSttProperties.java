package com.swpts.enpracticebe.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "stt.google")
public class GoogleSttProperties {

    private boolean enabled;
    private String projectId;
    private String location = "global";
    private String recognizer = "_";
    private List<String> languageCodes = List.of("en-US");
    private String model = "latest_long";
    private boolean interimResults = true;
    private String endpoint = "speech.googleapis.com:443";
    private String credentialsBase64;

    // ─── Enhanced analytics config ────────────────────────────────────────────
    /** Enable word-level time offsets from Google STT (needed for pause detection & WPM) */
    private boolean enableWordTimeOffsets = true;
    /** Enable word-level confidence scores from Google STT (needed for pronunciation analysis) */
    private boolean enableWordConfidence = true;
    /** Gap between words (ms) that is considered a meaningful pause */
    private int pauseThresholdMs = 1000;
    /** Word confidence below this value is flagged as potentially mispronounced */
    private float lowConfidenceThreshold = 0.75f;


    public String recognizerPath() {
        return String.format(
                "projects/%s/locations/%s/recognizers/%s",
                projectId, location, recognizer
        );
    }
}
