package com.bronx.telegram.notification.service.impl.webhookCommand.helper;

import com.bronx.telegram.notification.model.enumz.BotStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class FormatterUtils {

    public String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public String formatDate(Instant instant) {
        if (instant == null) return "N/A";
        try {
            return DateTimeFormatter
                    .ofPattern("MMM dd, yyyy HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
        } catch (Exception e) {
            return instant.toString();
        }
    }

    public String getStatusEmoji(BotStatus status) {
        if (status == null) return "⚪";
        switch (status) {
            case ACTIVE:
                return "🟢";
            case INACTIVE:
                return "🔴";
            case PENDING:
                return "🟡";
            case SUSPENDED:
                return "🟠";
            case TERMINATE:
                return "⚫";
            default:
                return "⚪";
        }
    }
}
