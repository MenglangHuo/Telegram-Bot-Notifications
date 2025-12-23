package com.bronx.telegram.notification.utils;

public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            return false;
        }
        String cleaned = contact.replaceAll("[^0-9+]", "");
        return cleaned.length() >= 8 && cleaned.length() <= 15;
    }
}
