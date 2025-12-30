package com.bronx.notification.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "slack")
@Data
public class SlackConfig {
    /**
     * Enable/disable Slack integration
     */
    private boolean enabled = true;
    /**
     * Slack webhook URL
     */
    private String webhookUrl;
    /**
     * Send messages asynchronously (non-blocking)
     */
    private boolean async = true;
    /**
     * Maximum retry attempts
     */
    private int maxRetries = 2;
    /**
     * Connection timeout in milliseconds
     */
    private int connectTimeout = 3000;
    /**
     * Read timeout in milliseconds
     */
    private int readTimeout = 5000;
}