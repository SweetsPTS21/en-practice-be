package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.constant.VoiceName;

public interface TextToSpeechService {

    /**
     * Synthesize text to speech audio bytes (MP3)
     *
     * @param text      the text to synthesize
     * @param voiceName optional voice name (e.g. "en-US-Neural2-J"), null for
     *                  default
     * @return MP3 audio bytes
     */
    byte[] synthesize(String text, VoiceName voiceName);

    /**
     * Synthesize a single vocabulary word to speech audio bytes (MP3).
     * Uses slower speaking rate for clearer pronunciation.
     *
     * @param word the vocabulary word to synthesize
     * @return MP3 audio bytes
     */
    byte[] synthesizeVocabulary(String word);
}
