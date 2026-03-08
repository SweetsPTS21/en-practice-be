package com.swpts.enpracticebe.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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
        } catch (Exception e) {
            // ignore
        }
        return new ArrayList<>();
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
