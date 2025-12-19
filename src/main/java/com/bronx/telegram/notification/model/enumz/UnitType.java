package com.bronx.telegram.notification.model.enumz;

public enum UnitType {
    ORGANIZATION,   // Root level - Company
    DIVISION,       // Major business unit
    DEPARTMENT,     // Functional department
    TEAM,           // Team within department
    OFFICE,         // Physical office/branch
    REGION,         // Geographic region
    COUNTRY,        // Country office
    PROJECT,        // Project-based unit
    CUSTOM          // Custom type
}
