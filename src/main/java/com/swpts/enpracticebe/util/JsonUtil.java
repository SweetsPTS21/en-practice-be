package com.swpts.enpracticebe.util;

import com.fasterxml.jackson.core.type.TypeReference;
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
}
