package com.swpts.enpracticebe.util;

import com.swpts.enpracticebe.entity.*;

import java.util.List;

public class PromptBuilder {

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

        return String.format("""
                        You are an IELTS Speaking examiner. Grade the following %s conversation.
                        
                        **Topic:** %s
                        
                        **Full Conversation Transcript:**
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
                transcript);
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

        return String.format("""
                        You are an IELTS Speaking examiner. Grade the following %s response.
                        
                        **Question:**
                        %s%s
                        
                        **Student's Transcript:**
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
                attempt.getTranscript());
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
}
