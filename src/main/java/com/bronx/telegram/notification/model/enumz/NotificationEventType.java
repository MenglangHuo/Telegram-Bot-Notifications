package com.bronx.telegram.notification.model.enumz;

public enum NotificationEventType {
    // Attendance
    CHECK_IN, CHECK_OUT, LATE_ARRIVAL, EARLY_DEPARTURE, MISSING_CHECKOUT,
    // Leave
    LEAVE_REQUEST, LEAVE_APPROVED, LEAVE_REJECTED,
    // Approvals
    APPROVAL_REQUEST, APPROVAL_REMINDER,
    // System
    SYSTEM_ALERT, DEADLINE_REMINDER, TEAM_ANNOUNCEMENT,
    // Custom
    CUSTOM_EVENT
}
