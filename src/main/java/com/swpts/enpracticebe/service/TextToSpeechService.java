package com.swpts.enpracticebe.service;

public interface TextToSpeechService {

    /**
     * Synthesize text to speech audio bytes (MP3)
     *
     * @param text      the text to synthesize
     * @param voiceName optional voice name (e.g. "en-US-Neural2-J"), null for
     *                  default
     * @return MP3 audio bytes
     */
    byte[] synthesize(String text, String voiceName);
}
