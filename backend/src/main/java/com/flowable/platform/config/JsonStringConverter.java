package com.flowable.platform.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter to handle String to JSON conversion for JSONB columns
 * This ensures proper type handling when storing strings in PostgreSQL JSONB columns
 */
@Converter(autoApply = false)
public class JsonStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return "{}";
        }
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return "{}";
        }
        return dbData;
    }
}
