package com.gearup.chatbotservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Service for post-processing chatbot responses to ensure conciseness and quality
 */
@Service
@Slf4j
public class ResponseProcessorService {

    @Value("${chatbot.response.max-length:500}")
    private int maxResponseLength;

    @Value("${chatbot.response.max-sentences:5}")
    private int maxSentences;

    @Value("${chatbot.response.remove-filler-words:true}")
    private boolean removeFillerWords;

    // Patterns for unnecessary phrases that make responses verbose
    private static final Pattern[] FILLER_PATTERNS = {
        Pattern.compile("I understand that you.*?\\. ", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Thank you for (asking|asking about|your question about|asking me about).*?\\. ", Pattern.CASE_INSENSITIVE),
        Pattern.compile("I'd be (happy|glad|pleased) to (help|assist)( you)?( with that)?\\. ?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Let me help you with that\\.? ?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("I appreciate (your|you) (question|asking).*?\\. ", Pattern.CASE_INSENSITIVE),
        Pattern.compile("That's a (great|good|excellent) question\\.? ?", Pattern.CASE_INSENSITIVE)
    };

    /**
     * Process and clean up a raw response from the LLM
     *
     * @param rawResponse the raw response from Ollama
     * @return processed, concise response
     */
    public String processResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return rawResponse;
        }

        String processed = rawResponse.trim();

        // Step 1: Remove unnecessary filler phrases
        if (removeFillerWords) {
            processed = removeFillerPhrases(processed);
        }

        // Step 2: Limit to max sentences
        processed = limitSentences(processed, maxSentences);

        // Step 3: Enforce character limit (but don't cut mid-sentence)
        processed = enforceCharacterLimit(processed, maxResponseLength);

        // Step 4: Clean up any formatting issues
        processed = cleanupFormatting(processed);

        log.debug("Processed response: {} chars -> {} chars",
            rawResponse.length(), processed.length());

        return processed;
    }

    /**
     * Remove common filler phrases that add verbosity without value
     */
    private String removeFillerPhrases(String text) {
        String result = text;

        for (Pattern pattern : FILLER_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }

        return result.trim();
    }

    /**
     * Limit response to first N sentences
     */
    private String limitSentences(String text, int maxSentences) {
        if (maxSentences <= 0) {
            return text;
        }

        // Split on sentence boundaries (., !, ?) followed by space or end of string
        String[] sentences = text.split("(?<=[.!?])\\s+");

        if (sentences.length <= maxSentences) {
            return text;
        }

        // Take only the first N sentences
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxSentences; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(sentences[i]);
        }

        return result.toString();
    }

    /**
     * Enforce character limit without cutting mid-sentence
     */
    private String enforceCharacterLimit(String text, int maxLength) {
        if (maxLength <= 0 || text.length() <= maxLength) {
            return text;
        }

        // Find the last sentence boundary before the limit
        int cutPoint = maxLength;

        // Look for the last period, exclamation, or question mark before the limit
        int lastPeriod = text.lastIndexOf('.', maxLength);
        int lastExclamation = text.lastIndexOf('!', maxLength);
        int lastQuestion = text.lastIndexOf('?', maxLength);

        cutPoint = Math.max(lastPeriod, Math.max(lastExclamation, lastQuestion));

        // If no sentence boundary found, cut at last space
        if (cutPoint <= 0 || cutPoint < maxLength * 0.5) {
            cutPoint = text.lastIndexOf(' ', maxLength);
            if (cutPoint > 0) {
                return text.substring(0, cutPoint) + "...";
            }
        }

        // Include the punctuation mark
        if (cutPoint > 0) {
            return text.substring(0, cutPoint + 1);
        }

        return text.substring(0, maxLength) + "...";
    }

    /**
     * Clean up formatting issues
     */
    private String cleanupFormatting(String text) {
        return text
            // Remove multiple consecutive spaces
            .replaceAll("\\s+", " ")
            // Remove space before punctuation
            .replaceAll("\\s+([.,!?])", "$1")
            // Ensure space after punctuation (if not at end)
            .replaceAll("([.,!?])([A-Za-z])", "$1 $2")
            // Remove any leading/trailing whitespace
            .trim();
    }

    /**
     * Count words in text
     */
    public int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /**
     * Count sentences in text
     */
    public int countSentences(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.split("[.!?]+").length;
    }
}
