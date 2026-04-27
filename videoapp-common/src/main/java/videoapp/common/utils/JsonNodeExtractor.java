package videoapp.common.utils;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeExtractor {

    public static String extractString(JsonNode node, String fieldName) {
        if (node.has(fieldName) && node.get(fieldName).isTextual()) {
            return node.get(fieldName).asText();
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' is missing or not a string");
    }

    public static Long extractLong(JsonNode node, String fieldName) {
        if (node.has(fieldName) && node.get(fieldName).isLong()) {
            return node.get(fieldName).asLong();
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' is missing or not a long");
    }

    public static Double extractDouble(JsonNode node, String fieldName) {
        if (node.has(fieldName) && node.get(fieldName).isDouble()) {
            return node.get(fieldName).asDouble();
        }
        throw new IllegalArgumentException("Field '" + fieldName + "' is missing or not a long");
    }
}
