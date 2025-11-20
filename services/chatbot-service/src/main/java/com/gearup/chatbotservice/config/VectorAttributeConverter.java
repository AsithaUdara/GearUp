package com.gearup.chatbotservice.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA AttributeConverter for handling PostgreSQL vector type
 * Converts between String representation and PGvector format
 */
@Converter
public class VectorAttributeConverter implements AttributeConverter<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(VectorAttributeConverter.class);

    @Override
    public Object convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        
        try {
            // Return as-is for pgvector to handle
            // The string should be in format "[1.0,2.0,3.0,...]"
            return attribute;
        } catch (Exception e) {
            logger.error("Error converting vector to database column: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid vector format: " + attribute, e);
        }
    }

    @Override
    public String convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            // Handle PGvector object
            if (dbData instanceof org.postgresql.util.PGobject) {
                org.postgresql.util.PGobject pgObject = (org.postgresql.util.PGobject) dbData;
                return pgObject.getValue();
            }
            
            // Handle String (for TEXT fallback)
            if (dbData instanceof String) {
                return (String) dbData;
            }
            
            // Handle other types by converting to string
            return dbData.toString();
        } catch (Exception e) {
            logger.error("Error converting vector from database: {}", e.getMessage());
            return null;
        }
    }
}
