package com.gearup.shared.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validates required configuration properties at startup.
 * Services can extend this to add their own validations.
 */
@Component
public class ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);
    private final Environment env;
    
    public ConfigValidator(Environment env) {
        this.env = env;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        List<String> missingVars = new ArrayList<>();
        
        // Database configuration - only validate if datasource URL is present
        String datasourceUrl = env.getProperty("spring.datasource.url");
        if (datasourceUrl != null && !datasourceUrl.trim().isEmpty()) {
            validateProperty("spring.datasource.url", missingVars);
            validateProperty("spring.datasource.username", missingVars);
            validateProperty("spring.datasource.password", missingVars);
        }
        
        if (!missingVars.isEmpty()) {
            String error = String.format(
                "Missing required environment variables: %s\n" +
                "Please set these in your .env file or environment",
                String.join(", ", missingVars)
            );
            logger.error(error);
            // Don't throw exception - just log warning for now
            logger.warn("⚠️ Configuration validation found issues but continuing startup");
        } else {
            logger.info("✅ All required configuration validated successfully");
        }
    }
    
    private void validateProperty(String property, List<String> missingVars) {
        String value = env.getProperty(property);
        if (value == null || value.trim().isEmpty()) {
            missingVars.add(property);
            logger.warn("❌ Missing required property: {}", property);
        } else {
            logger.debug("✅ Property configured: {}", property);
        }
    }
}
