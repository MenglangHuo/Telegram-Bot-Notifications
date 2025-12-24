package com.bronx.notification.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class JsonParserUtils {

    public static JsonNode get(JsonNode root, String... path) {
        JsonNode current = root;
        for (String p : path) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return MissingNode.getInstance();
            }
            current = current.path(p);
        }
        return current;
    }

    public static String text(JsonNode root, String... path) {
        JsonNode n = get(root, path);
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }

    public static Long longVal(JsonNode root, String... path) {
        JsonNode n = get(root, path);
        return n.isMissingNode() || n.isNull() ? null : n.asLong();
    }

}
