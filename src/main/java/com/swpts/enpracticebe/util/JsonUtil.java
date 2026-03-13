package com.swpts.enpracticebe.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<String> parseJsonList(Object jsonObj) {
        if (jsonObj == null)
            return new ArrayList<>();
        try {
            if (jsonObj instanceof String jsonStr) {
                return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {
                });
            }
            if (jsonObj instanceof List) {
                return (List<String>) jsonObj;
            }
            if (jsonObj instanceof JsonNode jsonNode) {
                return objectMapper.convertValue(jsonNode, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            // ignore
        }
        return new ArrayList<>();
    }

    public static <T> List<T> parseJsonList(Object jsonObj, Class<T> clazz) {
        if (jsonObj == null) {
            return Collections.emptyList();
        }

        try {
            CollectionType listType =
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);

            if (jsonObj instanceof String jsonStr) {
                return objectMapper.readValue(jsonStr, listType);
            }
            if (jsonObj instanceof JsonNode jsonNode) {
                return objectMapper.readerFor(listType).readValue(jsonNode);
            }

            return objectMapper.convertValue(jsonObj, listType);

        } catch (Exception e) {
            log.warn("Failed to parse json list. Input type: {}", jsonObj.getClass(), e);
            return Collections.emptyList();
        }
    }

    public static String extractJson(String text) {
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.indexOf("```", start);
            if (end > start) return text.substring(start, end).trim();
        }
        int braceStart = text.indexOf('{');
        int braceEnd = text.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1);
        }
        return text;
    }

    public static Float getFloatField(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return (float) node.get(field).asDouble();
        }
        return null;
    }
}
