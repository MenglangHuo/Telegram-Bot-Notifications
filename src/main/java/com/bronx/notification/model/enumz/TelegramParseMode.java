package com.bronx.notification.model.enumz;

public enum TelegramParseMode {
    NONE(""),
    HTML("HTML"),
    MARKDOWN("Markdown"),
    MARKDOWN_V2("MarkdownV2");

    private final String value;

    TelegramParseMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
