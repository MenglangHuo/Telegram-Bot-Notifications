package com.bronx.notification.utils;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component

public class HtmlToMarkdownConverter {
    private final FlexmarkHtmlConverter converter;

    public HtmlToMarkdownConverter() {
        this.converter = FlexmarkHtmlConverter.builder().build();
    }

    /**
     * Convert HTML to Markdown
     */
    public String convertHtmlToMarkdown(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        try {
            // Check if content contains HTML tags
            if (!containsHtmlTags(html)) {
                return html;
            }

            String markdown = converter.convert(html);

            // Convert to Slack mrkdwn format
            return convertToSlackMarkdown(markdown);
        } catch (Exception e) {
            log.warn("Failed to convert HTML to Markdown, using original: {}", e.getMessage());
            return html;
        }
    }

    /**
     * Check if string contains HTML tags
     */
    private boolean containsHtmlTags(String text) {
        return text.matches(".*<[^>]+>.*");
    }

    /**
     * Convert standard Markdown to Slack mrkdwn format
     */
    private String convertToSlackMarkdown(String markdown) {
        return markdown
                // Bold: **text** or __text__ -> *text*
                .replaceAll("\\*\\*([^*]+)\\*\\*", "*$1*")
                .replaceAll("__([^_]+)__", "*$1*")
                // Italic: *text* or _text_ -> _text_
                .replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "_$1_")
                // Strikethrough: ~~text~~ -> ~text~
                .replaceAll("~~([^~]+)~~", "~$1~")
                // Code: `code` stays the same
                // Links: [text](url) -> <url|text>
                .replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "<$2|$1>")
                // Headers: remove # symbols (Slack doesn't support headers in text)
                .replaceAll("#{1,6}\\s+", "*")
                .replaceAll("\\*([^*]+)", "*$1*\n")
                // Clean up extra newlines
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
