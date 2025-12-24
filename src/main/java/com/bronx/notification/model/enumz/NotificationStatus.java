package com.bronx.notification.model.enumz;

public enum NotificationStatus {
    QUEUED,         // Saved to DB, sent to RabbitMQ
    PROCESSING,     // Consumer picked it up
    DELIVERED,      // Successfully sent
    FAILED,         // All retries exhausted
    CANCELLED,       // Manually cancelled
    SENT
}
