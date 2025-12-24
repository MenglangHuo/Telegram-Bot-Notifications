package com.bronx.notification.model.enumz;

public enum NotificationPriority {
    LOW(4),
    NORMAL(3),
    HIGH(2),
    URGENT(1);

    private final int value;
    NotificationPriority(int value) { this.value = value; }
    public int getValue() { return value; }
}
