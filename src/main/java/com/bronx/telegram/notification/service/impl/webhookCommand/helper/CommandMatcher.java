package com.bronx.telegram.notification.service.impl.webhookCommand.helper;

import org.springframework.stereotype.Component;

@Component
public class CommandMatcher {

    public String normalizeCommand(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase();
    }

    public boolean matchesCommand(String normalizedText, String... patterns) {
        for (String pattern : patterns) {
            if (normalizedText.equals(pattern.toLowerCase()) ||
                    normalizedText.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
