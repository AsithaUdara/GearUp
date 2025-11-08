package com.gearup.chatbotservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Intent Classification Service
 * Classifies user intent using rule-based pattern matching
 */
@Service
@Slf4j
public class IntentClassifierService {
    
    private static final Map<String, Pattern[]> INTENT_PATTERNS = Map.of(
        "status", new Pattern[] {
            Pattern.compile(".*\\b(status|progress|check|track)\\b.*\\b(booking|appointment|service|car|vehicle)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(where|what).*\\b(is|are)\\b.*\\b(my|the)\\b.*\\b(booking|appointment|service|car|vehicle)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\bwhere.*\\bmy\\b.*\\b(car|vehicle)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "modify", new Pattern[] {
            Pattern.compile(".*\\b(change|modify|update|cancel|reschedule)\\b.*\\b(booking|appointment|service)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(need to|want to|would like to)\\b.*\\b(change|modify|reschedule|cancel)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(move|shift).*\\b(appointment|booking)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "booking", new Pattern[] {
            Pattern.compile(".*\\b(book|schedule|reserve)\\b.*\\b(appointment|service)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(want|need|would like).*\\b(to book|to schedule|to make).*\\b(appointment|service)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(make|set up|arrange).*\\b(an? )?(appointment|service|booking)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "pricing", new Pattern[] {
            Pattern.compile(".*\\b(price|cost|how much|charge|fees?|pricing|rate)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(what.*cost|what.*price|how expensive)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(afford|budget|payment)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "services", new Pattern[] {
            Pattern.compile(".*\\b(what.*service|which.*service|available.*service|offer|provide)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(types?.*service|kind.*service|service.*available)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(do you (have|offer|provide))\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "hours", new Pattern[] {
            Pattern.compile(".*\\b(hours?|timing|time|when.*open|opening|schedule)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(what time|when).*\\b(open|close|available)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(available.*when|open.*when)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "location", new Pattern[] {
            Pattern.compile(".*\\b(where|location|address|find|directions?)\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(how to (get|reach|find))\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(located|situated)\\b.*", Pattern.CASE_INSENSITIVE)
        },
        
        "greeting", new Pattern[] {
            Pattern.compile("^(hi|hello|hey|greetings|good\\s+(morning|afternoon|evening))\\b.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\b(how are you|how's it going)\\b.*", Pattern.CASE_INSENSITIVE)
        }
    );
    
    /**
     * Classify user intent from message
     * 
     * @param message user message
     * @return classified intent
     */
    public String classifyIntent(String message) {
        if (message == null || message.isBlank()) {
            return "information";
        }
        
        String normalizedMessage = message.trim();
        
        // Check each intent pattern
        for (Map.Entry<String, Pattern[]> entry : INTENT_PATTERNS.entrySet()) {
            String intent = entry.getKey();
            Pattern[] patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                if (pattern.matcher(normalizedMessage).matches()) {
                    log.info("Classified intent '{}' for message: {}", intent, 
                        message.length() > 50 ? message.substring(0, 50) + "..." : message);
                    return intent;
                }
            }
        }
        
        // Default intent
        log.debug("No specific intent matched, defaulting to 'information'");
        return "information";
    }
    
    /**
     * Get confidence score for a specific intent (0-1)
     * Higher score = higher confidence
     * 
     * @param message user message
     * @param targetIntent intent to check
     * @return confidence score
     */
    public double getIntentConfidence(String message, String targetIntent) {
        Pattern[] patterns = INTENT_PATTERNS.get(targetIntent);
        if (patterns == null) {
            return 0.0;
        }
        
        int matchCount = 0;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(message).matches()) {
                matchCount++;
            }
        }
        
        // Simple confidence based on number of pattern matches
        return (double) matchCount / patterns.length;
    }
    
    /**
     * Check if message matches multiple intents
     * 
     * @param message user message
     * @return map of intent to confidence scores
     */
    public Map<String, Double> getMultipleIntents(String message) {
        Map<String, Double> intentScores = new java.util.HashMap<>();
        
        for (String intent : INTENT_PATTERNS.keySet()) {
            double confidence = getIntentConfidence(message, intent);
            if (confidence > 0.0) {
                intentScores.put(intent, confidence);
            }
        }
        
        return intentScores;
    }
}
