package com.flowable.platform.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

/**
 * Utility for loading test fixtures from classpath resources.
 */
public class FixtureLoader {

    private final ObjectMapper objectMapper;

    public FixtureLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Load fixture from JSON file
     */
    public <T> T loadJson(String path, Class<T> clazz) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return objectMapper.readValue(is, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture: " + path, e);
        }
    }

    /**
     * Load multiple fixtures from JSON array file
     */
    public <T> List<T> loadJsonArray(String path, Class<T> clazz) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return objectMapper.readValue(
                is,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture array: " + path, e);
        }
    }

    /**
     * Load JSON string from file
     */
    public String loadJsonString(String path) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture string: " + path, e);
        }
    }
}
