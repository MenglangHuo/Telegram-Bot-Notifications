package com.bronx.notification.dto.notification;

public record MediaMetaData(
        String title,
        String performer,
        Integer duration,
        String fileName
) {
}
