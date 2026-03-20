package com.swpts.enpracticebe.util;

import com.swpts.enpracticebe.constant.CustomConversationExpertise;
import com.swpts.enpracticebe.constant.CustomConversationPersonality;
import com.swpts.enpracticebe.constant.CustomConversationStyle;
import com.swpts.enpracticebe.entity.*;

import java.util.List;
import java.util.Locale;

public class PromptBuilder {

    public static String buildExplainWordPrompt(String word) {
        return String.format("""
                Giải thích từ: "%s"
                
                1. Phiên âm IPA
                2. Loại từ
                3. Nghĩa tiếng việt
                4. Ví dụ câu (En + Vi)
                
                Phải trả về nội dung dưới dạng json dưới đây, không thêm gì khác
                
                {
                    "word": bubble,
                    "ipa": "/ˈbʌb.əl/",
                    "wordType": "noun,verb",
                    "meaning": "(n): bong bóng, bọt khí, (v): sủi bọt, nói líu lo",
                    "examples": [
                        "sentence": "The soup started to bubble—and my patience started to bubble too.",
                        "translation": "Nồi súp bắt đầu sủi bọt—và kiên nhẫn của mình cũng sủi bọt luôn.",
                    ],
                    "sourceType": "VOCABULARY",
                    "explanation": ""
                }
                
                Lưu ý:
                - Ví dụ 1-2 câu kèm nghĩa tiếng việt
                - Explanation sẽ là những lưu ý, lỗi dễ mắc phải khi dùng từ (md format)
                """, word);
    }

    public static String buildConversationGradingPrompt(SpeakingTopic topic, List<SpeakingConversationTurn> turns) {
        StringBuilder transcript = new StringBuilder();
        for (SpeakingConversationTurn turn : turns) {
            transcript.append("Examiner: ").append(turn.getAiQuestion()).append("\n");
            if (turn.getUserTranscript() != null) {
                transcript.append("Student: ").append(turn.getUserTranscript()).append("\n");
            }
            transcript.append("\n");
        }

        String partDesc = switch (topic.getPart()) {
            case PART_1 -> "IELTS Speaking Part 1 (familiar topics, short answers)";
            case PART_2 -> "IELTS Speaking Part 2 (individual long turn / cue card)";
            case PART_3 -> "IELTS Speaking Part 3 (discussion, abstract ideas)";
        };

        // Aggregate analytics across all turns that have data
        List<SpeakingConversationTurn> turnsWithAnalytics = turns.stream()
                .filter(t -> t.getWordCount() != null && t.getWordCount() > 0)
                .toList();

        String analyticsSection = "";
        if (!turnsWithAnalytics.isEmpty()) {
            double avgWpm = turnsWithAnalytics.stream()
                    .filter(t -> t.getWordsPerMinute() != null)
                    .mapToDouble(SpeakingConversationTurn::getWordsPerMinute)
                    .average().orElse(0);
            int totalPauses = turnsWithAnalytics.stream()
                    .filter(t -> t.getPauseCount() != null)
                    .mapToInt(SpeakingConversationTurn::getPauseCount).sum();
            int totalLongPauses = turnsWithAnalytics.stream()
                    .filter(t -> t.getLongPauseCount() != null)
                    .mapToInt(SpeakingConversationTurn::getLongPauseCount).sum();
            int totalFillers = turnsWithAnalytics.stream()
                    .filter(t -> t.getFillerWordCount() != null)
                    .mapToInt(SpeakingConversationTurn::getFillerWordCount).sum();
            double avgConf = turnsWithAnalytics.stream()
                    .filter(t -> t.getAvgWordConfidence() != null)
                    .mapToDouble(SpeakingConversationTurn::getAvgWordConfidence)
                    .average().orElse(0);

            analyticsSection = buildSpeakingAnalyticsSection(
                    avgWpm, totalPauses, totalLongPauses, null, totalFillers, avgConf, null);
        }

        return String.format("""
                        You are an IELTS Speaking examiner. Grade the following %s conversation.
                        
                        **Topic:** %s
                        
                        **Full Conversation Transcript:**
                        %s
                        %s
                        Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                        1. Fluency and Coherence
                        2. Lexical Resource
                        3. Grammatical Range and Accuracy
                        4. Pronunciation
                        
                        Also provide detailed feedback covering:
                        - Strengths in the conversation
                        - Areas for improvement
                        - Specific examples from the transcript
                        - Tips for achieving a higher band score
                        
                        You MUST respond in the following JSON format only, no extra text:
                        {
                          "fluency": 6.5,
                          "lexical": 6.0,
                          "grammar": 6.5,
                          "pronunciation": 6.0,
                          "overall_band": 6.5,
                          "feedback": "Your detailed feedback in markdown format here..."
                        }
                        """,
                partDesc,
                topic.getQuestion(),
                transcript,
                analyticsSection);
    }

    public static String buildSpeakingGradingPrompt(SpeakingTopic topic, SpeakingAttempt attempt) {
        String customPrompt = topic.getAiGradingPrompt();
        if (customPrompt != null && !customPrompt.isBlank()) {
            return customPrompt
                    .replace("{transcript}", attempt.getTranscript())
                    .replace("{question}", topic.getQuestion())
                    .replace("{part}", topic.getPart().name());
        }

        String partDesc = switch (topic.getPart()) {
            case PART_1 -> "IELTS Speaking Part 1 (familiar topics, short answers)";
            case PART_2 -> "IELTS Speaking Part 2 (individual long turn / cue card)";
            case PART_3 -> "IELTS Speaking Part 3 (discussion, abstract ideas)";
        };

        String cueCardSection = topic.getCueCard() != null && !topic.getCueCard().isBlank()
                ? "\n\n**Cue Card:**\n" + topic.getCueCard()
                : "";

        // Build speech analytics section if data is available
        String analyticsSection = buildSpeakingAnalyticsSection(
                attempt.getWordsPerMinute(),
                attempt.getPauseCount(),
                attempt.getLongPauseCount(),
                attempt.getAvgPauseDurationMs(),
                attempt.getFillerWordCount(),
                attempt.getAvgWordConfidence(),
                null // low confidence words not stored separately — in speechDataJson
        );

        return String.format("""
                        You are an IELTS Speaking examiner. Grade the following %s response.
                        
                        **Question:**
                        %s%s
                        
                        **Student's Transcript:**
                        %s
                        %s
                        Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                        1. Fluency and Coherence
                        2. Lexical Resource
                        3. Grammatical Range and Accuracy
                        4. Pronunciation
                        
                        You MUST respond in the following JSON format only, no extra text:
                        {
                          "fluency": 6.5,
                          "lexical": 6.0,
                          "grammar": 6.5,
                          "pronunciation": 6.0,
                          "overall_band": 6.5,
                          "feedback": "Your detailed feedback in markdown format here..."
                        }
                        """,
                partDesc,
                topic.getQuestion(),
                cueCardSection,
                attempt.getTranscript(),
                analyticsSection);
    }

    /**
     * Builds the analytics context block injected into grading prompts.
     * All params are nullable — only non-null values are included.
     */
    private static String buildSpeakingAnalyticsSection(
            Double wpm, Integer pauseCount, Integer longPauseCount,
            Double avgPauseDurationMs, Integer fillerWordCount,
            Double avgWordConfidence, List<String> lowConfidenceWords) {

        boolean hasAnyData = (wpm != null && wpm > 0)
                || (pauseCount != null)
                || (fillerWordCount != null)
                || (avgWordConfidence != null && avgWordConfidence > 0);

        if (!hasAnyData) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n**Speech Analytics (auto-measured from audio):**\n");

        if (wpm != null && wpm > 0) {
            String rateComment = wpm < 100 ? "slow — student may be hesitating"
                    : wpm < 110 ? "slightly below target"
                    : wpm <= 150 ? "good (IELTS target: 110-150 WPM)"
                    : "fast — may affect clarity";
            sb.append(String.format("- Speaking rate: %.1f WPM (%s)%n", wpm, rateComment));
        }

        if (pauseCount != null) {
            sb.append(String.format("- Pauses detected: %d total", pauseCount));
            if (longPauseCount != null && longPauseCount > 0) {
                sb.append(String.format(" (%d long pauses > 2s)", longPauseCount));
            }
            sb.append("\n");
        }

        if (fillerWordCount != null && fillerWordCount > 0) {
            sb.append(String.format("- Filler words: %d occurrences (um, uh, like, etc.) — reflects hesitation%n",
                    fillerWordCount));
        }

        if (avgWordConfidence != null && avgWordConfidence > 0) {
            String confComment = avgWordConfidence >= 0.9 ? "clear pronunciation"
                    : avgWordConfidence >= 0.75 ? "generally clear, some unclear words"
                    : "many words unclear to ASR — pronunciation needs improvement";
            sb.append(String.format("- ASR word confidence: %.2f/1.0 (%s)%n", avgWordConfidence, confComment));
        }

        if (lowConfidenceWords != null && !lowConfidenceWords.isEmpty()) {
            sb.append(String.format("- Potentially mispronounced words (low ASR confidence): %s%n",
                    String.join(", ", lowConfidenceWords)));
        }

        sb.append("Use the analytics above to provide more specific feedback on Fluency and Pronunciation.\n");
        return sb.toString();
    }

    public static String buildWritingGradingPrompt(WritingTask task, WritingSubmission submission) {
        String customPrompt = task.getAiGradingPrompt();
        if (customPrompt != null && !customPrompt.isBlank()) {
            return customPrompt
                    .replace("{essay}", submission.getEssayContent())
                    .replace("{task_content}", task.getContent())
                    .replace("{task_type}", task.getTaskType().name());
        }

        String taskTypeDesc = task.getTaskType() == WritingTask.TaskType.TASK_1
                ? "IELTS Writing Task 1 (describe a chart/graph/process/map)"
                : "IELTS Writing Task 2 (essay)";

        return String.format("""
                        You are an IELTS Writing examiner. Grade the following %s submission.
                        
                        **Task/Question:**
                        %s
                        
                        **Student's Essay (%d words):**
                        %s
                        
                        Grade on these 4 criteria (each 0.0 to 9.0, in 0.5 increments):
                        1. Task Response (TR)
                        2. Coherence and Cohesion (CC)
                        3. Lexical Resource (LR)
                        4. Grammatical Range and Accuracy (GRA)
                        
                        You MUST respond in the following JSON format only, no extra text:
                        {
                          "task_response": 6.5,
                          "coherence": 6.0,
                          "lexical_resource": 6.5,
                          "grammar": 6.0,
                          "overall_band": 6.5,
                          "feedback": "Your detailed feedback in markdown format here..."
                        }
                        """,
                taskTypeDesc,
                task.getContent(),
                submission.getWordCount(),
                submission.getEssayContent());
    }

    public static String buildAdaptivePrompt(SpeakingTopic topic,
                                             List<SpeakingConversationTurn> existingTurns,
                                             SpeakingConversationTurn currentTurn,
                                             String nextFollowUp) {
        StringBuilder history = new StringBuilder();
        for (SpeakingConversationTurn turn : existingTurns) {
            String label = "HINT".equals(turn.getTurnType()) ? "Examiner (hint)" : "Examiner";
            history.append(label).append(": ").append(turn.getAiQuestion()).append("\n");
            if (turn.getUserTranscript() != null) {
                history.append("Student: ").append(turn.getUserTranscript()).append("\n");
            }
        }

        String nextFollowUpSection = nextFollowUp != null
                ? "**Next follow-up question to move to (if student is ready):** " + nextFollowUp
                : "**No more follow-up questions remaining** — if the student answered well, this is the final turn.";

        return String.format("""
                        You are a friendly, professional IELTS Speaking examiner conducting a live interview.
                        
                        **Topic:** %s
                        **Part:** %s
                        
                        **Conversation so far:**
                        %s
                        
                        **Student's latest answer:** %s
                        
                        %s
                        
                        Your task: Analyze the student's latest answer and decide ONE of these actions:
                        
                        **Action "HINT"** — Choose this if:
                        - The student's answer is very short (only a few words), vague, or off-topic
                        - The student seems to be struggling, hesitating, or says phrases like "I don't know", \
                        "I'm not sure", "um...", or gives a very generic answer
                        - The student explicitly asks for help or seems confused
                        
                        If you choose HINT:
                        - Encourage the student warmly and naturally (e.g., "That's okay, no worries!", "Good start!")
                        - Give them a helpful hint or suggestion to guide their thinking \
                        (e.g., "Think about a time when...", "You could talk about...", "Maybe consider...")
                        - End your response by naturally asking something like: \
                        "Would you like me to give you another hint, or are you ready to give it a try?"
                        - Keep it friendly, encouraging, and slightly humorous
                        
                        **Action "FOLLOWUP"** — Choose this if:
                        - The student gave a reasonable, substantive answer (even if imperfect)
                        - The student indicates they're ready to move on or ready to answer
                        - The student responded to a hint by giving a proper answer
                        
                        If you choose FOLLOWUP:
                        - Briefly comment on their answer naturally (1-2 sentences, can be witty/humorous)
                        - Then smoothly transition to the next follow-up question
                        - Adapt the question naturally so it flows from the conversation
                        
                        IMPORTANT: Respond with EXACTLY this JSON format, nothing else:
                        {"action": "HINT" or "FOLLOWUP", "response": "your spoken response here"}
                        
                        Rules for the "response" field:
                        - Natural spoken English, as if having a real conversation
                        - No prefixes like "Examiner:" or "Question:"
                        - No markdown formatting
                        - Concise (3-5 sentences max)
                        """,
                topic.getQuestion(),
                topic.getPart().name(),
                history,
                currentTurn.getUserTranscript(),
                nextFollowUpSection);
    }

    public static String buildCustomConversationStartPrompt(String topic,
                                                            CustomConversationStyle style,
                                                            CustomConversationPersonality personality,
                                                            CustomConversationExpertise expertise) {
        return String.format("""
                        You are preparing a live English speaking conversation.
                        The exact text you write will be used as a spoken transcript for Google TTS, so it must sound natural out loud.

                        Conversation requirements:
                        - Topic: %s
                        - Speaking style: %s
                        - AI personality: %s
                        - AI expertise: %s

                        Your task:
                        1. Create a short, natural conversation title in English (max 6 words).
                        2. Write the first AI message that opens the conversation naturally.

                        Rules for the opening message:
                        - Sound like a real conversation partner, not a teacher giving instructions
                        - Stay aligned with the requested style, personality, and expertise
                        - Write for speech, not for reading: use contractions, natural pauses with punctuation, and varied sentence lengths
                        - Let the wording show light emotion when it fits: curiosity, warmth, surprise, confidence, excitement, or empathy
                        - You may use occasional spoken markers such as "well," "oh," "hmm," "honestly," or "right," but only when they feel natural
                        - Keep it concise: 2-4 sentences, around 20-60 words total
                        - End with a question that invites the user to respond
                        - Use English only
                        - No markdown, no bullet points, no emojis, no stage directions, no speaker labels, and no quotation marks around the whole message
                        %s

                        Respond with EXACTLY this JSON format:
                        {
                          "title": "Short title here",
                          "opening_message": "First AI message here"
                        }
                        """,
                topic,
                humanizeEnumValue(style),
                humanizeEnumValue(personality),
                humanizeEnumValue(expertise),
                buildCustomConversationToneGuidance(style, personality, expertise, false, 0));
    }

    public static String buildCustomConversationReplyPrompt(String topic,
                                                            CustomConversationStyle style,
                                                            CustomConversationPersonality personality,
                                                            CustomConversationExpertise expertise,
                                                            List<CustomSpeakingConversationTurn> turns,
                                                            String latestUserAnswer,
                                                            int remainingUserTurns) {
        StringBuilder history = new StringBuilder();
        for (CustomSpeakingConversationTurn turn : turns) {
            history.append("AI: ").append(turn.getAiMessage()).append("\n");
            if (turn.getUserTranscript() != null && !turn.getUserTranscript().isBlank()) {
                history.append("User: ").append(turn.getUserTranscript()).append("\n");
            }
        }

        return String.format("""
                        You are an AI conversation partner having a spoken English practice conversation.
                        The exact text you write will be used directly as a spoken transcript for Google TTS, so it must sound excellent when spoken aloud.

                        Conversation setup:
                        - Topic: %s
                        - Style: %s
                        - Personality: %s
                        - Expertise: %s
                        - Remaining user turns before auto-stop: %d

                        Conversation history:
                        %s

                        User's latest answer:
                        %s

                        Your task:
                        - Reply naturally in English
                        - Keep continuity with the conversation history
                        - Stay aligned with the requested style, personality, and expertise
                        - Sound like one real, emotionally present human speaking to another
                        - Write for speech, not for reading: use contractions, natural pauses with punctuation, and varied sentence lengths
                        - Let the wording show light emotion or attitude when it fits: curiosity, warmth, surprise, confidence, doubt, excitement, or empathy
                        - You may use occasional spoken markers such as "well," "oh," "hmm," "honestly," "right," "wait," or "I mean," but do not force them and do not use them in every sentence
                        - React to what the user actually said instead of giving generic praise
                        - Encourage the user to keep speaking
                        - End with a natural follow-up question unless the conversation already feels complete
                        - If only 1 user turn remains, make the follow-up feel like a satisfying closing turn
                        - Keep it concise: 2-4 sentences, around 25-70 words total
                        - Make it clean for TTS: no markdown, no bullet points, no emojis, no stage directions, no speaker labels, no slash-separated options, and no quotation marks around the whole response
                        - Avoid robotic, overly formal, or repetitive teacher-like phrasing
                        %s

                        Respond with EXACTLY this JSON format:
                        {
                          "response": "Your next AI message here"
                        }
                        """,
                topic,
                humanizeEnumValue(style),
                humanizeEnumValue(personality),
                humanizeEnumValue(expertise),
                remainingUserTurns,
                history,
                latestUserAnswer,
                buildCustomConversationToneGuidance(style, personality, expertise, true, remainingUserTurns));
    }

    public static String buildCustomConversationGradingPrompt(String topic,
                                                              String title,
                                                              List<CustomSpeakingConversationTurn> turns) {
        StringBuilder transcript = new StringBuilder();
        for (CustomSpeakingConversationTurn turn : turns) {
            transcript.append("AI: ").append(turn.getAiMessage()).append("\n");
            if (turn.getUserTranscript() != null && !turn.getUserTranscript().isBlank()) {
                transcript.append("User: ").append(turn.getUserTranscript()).append("\n");
            }
            transcript.append("\n");
        }

        List<CustomSpeakingConversationTurn> turnsWithAnalytics = turns.stream()
                .filter(t -> t.getWordCount() != null && t.getWordCount() > 0)
                .toList();

        String analyticsSection = "";
        if (!turnsWithAnalytics.isEmpty()) {
            double avgWpm = turnsWithAnalytics.stream()
                    .filter(t -> t.getWordsPerMinute() != null)
                    .mapToDouble(CustomSpeakingConversationTurn::getWordsPerMinute)
                    .average().orElse(0);
            int totalPauses = turnsWithAnalytics.stream()
                    .filter(t -> t.getPauseCount() != null)
                    .mapToInt(CustomSpeakingConversationTurn::getPauseCount).sum();
            int totalLongPauses = turnsWithAnalytics.stream()
                    .filter(t -> t.getLongPauseCount() != null)
                    .mapToInt(CustomSpeakingConversationTurn::getLongPauseCount).sum();
            int totalFillers = turnsWithAnalytics.stream()
                    .filter(t -> t.getFillerWordCount() != null)
                    .mapToInt(CustomSpeakingConversationTurn::getFillerWordCount).sum();
            double avgConf = turnsWithAnalytics.stream()
                    .filter(t -> t.getAvgWordConfidence() != null)
                    .mapToDouble(CustomSpeakingConversationTurn::getAvgWordConfidence)
                    .average().orElse(0);

            analyticsSection = buildSpeakingAnalyticsSection(
                    avgWpm, totalPauses, totalLongPauses, null, totalFillers, avgConf, null);
        }

        return String.format("""
                        You are evaluating an English speaking practice conversation.

                        Conversation title: %s
                        Topic: %s

                        Full transcript:
                        %s
                        %s
                        Score the user's speaking performance using these general criteria (0.0 to 10.0, allow 0.5 increments):
                        1. Fluency: how smoothly the user speaks and keeps going
                        2. Vocabulary: range and appropriateness of words used
                        3. Coherence: how clearly and logically the user expresses ideas
                        4. Pronunciation: how understandable the spoken English is

                        Also provide:
                        - A brief summary of the user's overall performance
                        - Key strengths
                        - Main improvement areas
                        - 3 practical next-step tips

                        Respond with EXACTLY this JSON format:
                        {
                          "fluency": 7.0,
                          "vocabulary": 6.5,
                          "coherence": 7.0,
                          "pronunciation": 6.5,
                          "overall_score": 7.0,
                          "feedback": "Markdown feedback here"
                        }
                        """,
                title,
                topic,
                transcript,
                analyticsSection);
    }

    public static String humanizeEnumValue(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String normalized = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private static String buildCustomConversationToneGuidance(CustomConversationStyle style,
                                                              CustomConversationPersonality personality,
                                                              CustomConversationExpertise expertise,
                                                              boolean replyMode,
                                                              int remainingUserTurns) {
        StringBuilder guidance = new StringBuilder();
        guidance.append("Additional tone guidance:\n");

        if (style == CustomConversationStyle.PROFESSIONAL
                || expertise == CustomConversationExpertise.BUSINESS
                || expertise == CustomConversationExpertise.EDUCATION) {
            guidance.append("- Keep the wording polished, composed, and socially appropriate.\n");
        }

        if (style == CustomConversationStyle.PLAYFUL
                || personality == CustomConversationPersonality.HUMOROUS
                || expertise == CustomConversationExpertise.ENTERTAINMENT) {
            guidance.append("- Add light banter, playful reactions, or cheeky phrasing when it feels natural.\n");
        }

        if (style == CustomConversationStyle.FLIRTY
                || personality == CustomConversationPersonality.FLIRTY
                || expertise == CustomConversationExpertise.RELATIONSHIPS) {
            guidance.append("- You can sound charming, teasing, and a little flirty, but keep it consensual and not explicit.\n");
        }

        if (style == CustomConversationStyle.DEEP
                || expertise == CustomConversationExpertise.PSYCHOLOGY) {
            guidance.append("- Ask more reflective, emotionally aware questions and sound thoughtful.\n");
        }

        if (style == CustomConversationStyle.DEBATE
                || style == CustomConversationStyle.CHALLENGING
                || personality == CustomConversationPersonality.BOLD
                || personality == CustomConversationPersonality.STRAIGHTFORWARD) {
            guidance.append("- It is fine to be more direct, skeptical, or provocative, as long as the reply still feels conversational.\n");
        }

        if (style == CustomConversationStyle.STREET
                || style == CustomConversationStyle.UNFILTERED
                || personality == CustomConversationPersonality.EDGY
                || personality == CustomConversationPersonality.REBELLIOUS
                || expertise == CustomConversationExpertise.GAMING
                || expertise == CustomConversationExpertise.SOCIAL_MEDIA
                || expertise == CustomConversationExpertise.STREET_CULTURE) {
            guidance.append("- You may occasionally use slang, blunt phrasing, or very light profanity such as \"damn,\" \"hell,\" or \"crap\" if it genuinely improves realism.\n");
            guidance.append("- Keep profanity sparse and natural. Never use slurs, hate speech, threats, or abusive harassment.\n");
        } else {
            guidance.append("- Avoid profanity and keep the wording clean.\n");
        }

        if (personality == CustomConversationPersonality.EMPATHETIC
                || personality == CustomConversationPersonality.PATIENT) {
            guidance.append("- Let the reply feel warm, emotionally attuned, and supportive.\n");
        }

        if (personality == CustomConversationPersonality.CONFIDENT) {
            guidance.append("- Speak with certainty and strong opinions when reacting, without sounding arrogant.\n");
        }

        if (personality == CustomConversationPersonality.CURIOUS) {
            guidance.append("- Sound genuinely intrigued and ask sharper follow-up questions.\n");
        }

        if (replyMode && remainingUserTurns == 1) {
            guidance.append("- This is the setup for the user's final turn, so make the question feel more memorable, personal, or high-payoff.\n");
        }

        return guidance.toString().trim();
    }
}
