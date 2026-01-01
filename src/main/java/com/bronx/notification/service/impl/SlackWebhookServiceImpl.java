package com.bronx.notification.service.impl;

import com.bronx.notification.configs.SlackConfig;
import com.bronx.notification.dto.notification.NotificationDetailProjection;
import com.bronx.notification.model.entity.Notification;
import com.bronx.notification.repository.NotificationRepository;
import com.bronx.notification.service.SlackWebhookService;
import com.bronx.notification.utils.HtmlToMarkdownConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class SlackWebhookServiceImpl implements SlackWebhookService {

    private final SlackConfig slackConfig;
    private final WebClient slackWebClient;
    private final HtmlToMarkdownConverter htmlToMarkdownConverter;
    private final NotificationRepository notificationRepository;

    /**
     * Send notification summary to Slack (async)
     */
    @Async("slackExecutor")
    public void sendNotificationToSlack(Notification notification) {
        if (!isEnabled()) return;
        try {
            Map<String, Object> payload = buildEnhancedSlackPayload(notification);
            sendWebhookAsync(payload).subscribe();
        } catch (Exception e) {
            log.error("Failed to send Slack notification: {}", e.getMessage());
        }
    }

    /**
     * Send notification summary to Slack (sync)
     */
    public void sendNotificationToSlackSync(Notification notification) {
        if (!isEnabled()) {
            return;
        }
        try {
            Map<String, Object> payload = buildEnhancedSlackPayload(notification);
            sendWebhookSync(payload);
        } catch (Exception e) {
            log.error("Failed to send Slack notification: {}", e.getMessage());
        }
    }

    /**
     * Send custom message to Slack (async)
     */
    @Async("slackExecutor")
    public void sendMessage(String message) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", message);
        sendWebhookAsync(payload).subscribe();
    }

    private boolean isEnabled() {
        if (!slackConfig.isEnabled() || slackConfig.getWebhookUrl() == null) {
            log.debug("Slack integration disabled, skipping notification");
            return false;
        }
        return true;
    }
    /**
     * Build Slack payload from notification
     */
    private Map<String, Object> buildEnhancedSlackPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();

        // Fetch enriched details
        Optional<NotificationDetailProjection> detailOpt =
                notificationRepository.findNotificationDetailById(notification.getId());

        // Fallback text
        payload.put("text", String.format("📬 Telegram notification sent to %s", notification.getChartId()));

        // Build blocks for rich formatting
        List<Map<String, Object>> blocks = new ArrayList<>();

        // Header block with status indicator
        blocks.add(createHeaderBlock(notification));

        // Business context section (Partner/Company/Subscription)
        if (detailOpt.isPresent()) {
            blocks.add(createBusinessContextBlock(detailOpt.get()));
        }

        // Technical details section
        blocks.add(createTechnicalDetailsBlock(notification));

//        // Message preview with HTML to Markdown conversion
//        if (notification.getMessage() != null && !notification.getMessage().isEmpty()) {
//            blocks.add(createMessagePreviewBlock(notification));
//        }

        // Divider
        Map<String, Object> divider = new HashMap<>();
        divider.put("type", "divider");
        blocks.add(divider);

        // Footer with timestamp
        blocks.add(createFooterBlock(notification));

        payload.put("blocks", blocks);
        return payload;
    }
    /**
     * Send webhook request (reactive)
     */
    private Mono<String> sendWebhookAsync(Map<String, Object> payload) {
        return slackWebClient.post()
                .uri(slackConfig.getWebhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("Slack webhook sent successfully"))
                .doOnError(error -> log.error("Slack webhook failed: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Slack API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Slack webhook error: {}", ex.getMessage());
                    return Mono.empty();
                });
    }
    /**
     * Create header block with status emoji
     */
    private Map<String, Object> createHeaderBlock(Notification notification) {
        Map<String, Object> headerBlock = new HashMap<>();
        headerBlock.put("type", "header");

        Map<String, Object> headerText = new HashMap<>();
        headerText.put("type", "plain_text");

        String emoji = getStatusEmoji(notification.getStatus());
        headerText.put("text", emoji + " Telegram Notification " + notification.getStatus());
        headerText.put("emoji", true);

        headerBlock.put("text", headerText);
        return headerBlock;
    }

    /**
     * Create business context block
     */
    private Map<String, Object> createBusinessContextBlock(NotificationDetailProjection detail) {
        Map<String, Object> contextBlock = new HashMap<>();
        contextBlock.put("type", "section");

        Map<String, Object> contextText = new HashMap<>();
        contextText.put("type", "mrkdwn");
        contextText.put("text", String.format(
                "*🏢 Business Context*\n" +
                        "• *Partner:* %s\n" +
                        "• *Company:* %s\n" +
                        "• *Organization:* %s\n" +
                        "• *Subscription:* %s\n" +
                        "• *Remaining Credits:* %d",
                detail.getPartner(),
                detail.getCompany(),
                detail.getOrganizationName(),
                detail.getSubscriptionName(),
                detail.getRemainCredit()
        ));

        contextBlock.put("text", contextText);
        return contextBlock;
    }

    /**
     * Create technical details block
     */
    private Map<String, Object> createTechnicalDetailsBlock(Notification notification) {
        Map<String, Object> sectionBlock = new HashMap<>();
        sectionBlock.put("type", "section");

        Map<String, Object> sectionText = new HashMap<>();
        sectionText.put("type", "mrkdwn");

        StringBuilder details = new StringBuilder("*🤖 Technical Details*\n");
        details.append(String.format("• *Bot:* `%s`\n", notification.getBotUsername()));
        details.append(String.format("• *Chat ID:* `%s`\n", notification.getChartId()));
        details.append(String.format("• *Type:* %s\n", notification.getType()));

        if (notification.getTelegramParseMode() != null) {
            details.append(String.format("• *Parse Mode:* %s\n", notification.getTelegramParseMode()));
        }

        if (notification.getTelegramMessageId() != null) {
            details.append(String.format("• *Message ID:* `%s`", notification.getTelegramMessageId()));
        }

        sectionText.put("text", details.toString());
        sectionBlock.put("text", sectionText);
        return sectionBlock;
    }

    /**
     * Create message preview block with HTML to Markdown conversion
     */
    private Map<String, Object> createMessagePreviewBlock(Notification notification) {
        Map<String, Object> messageBlock = new HashMap<>();
        messageBlock.put("type", "section");

        Map<String, Object> messageText = new HashMap<>();
        messageText.put("type", "mrkdwn");

        String message = notification.getMessage();

        // Convert HTML to Markdown if parseMode is HTML
        if ("HTML".equalsIgnoreCase(String.valueOf(notification.getTelegramParseMode()))) {
            message = htmlToMarkdownConverter.convertHtmlToMarkdown(message);
        }

        // Truncate if too long
        String preview = message.length() > 500
                ? message.substring(0, 500) + "..."
                : message;

        messageText.put("text", String.format("*📝 Message Preview:*\n```%s```", preview));
        messageBlock.put("text", messageText);
        return messageBlock;
    }

    /**
     * Create footer with timestamp
     */
    private Map<String, Object> createFooterBlock(Notification notification) {
        Map<String, Object> contextBlock = new HashMap<>();
        contextBlock.put("type", "context");

        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> element = new HashMap<>();
        element.put("type", "mrkdwn");
        element.put("text", String.format(
                "Notification ID: `%d` | Sent: <!date^%d^{date_short_pretty} at {time}|%s>",
                notification.getId(),
                notification.getSentAt().getEpochSecond(),
                notification.getSentAt().toString()
        ));
        elements.add(element);

        contextBlock.put("elements", elements);
        return contextBlock;
    }

    /**
     * Get status emoji
     */
    private String getStatusEmoji(Object status) {
        String statusStr = status.toString();
        return switch (statusStr) {
            case "SENT" -> "✅";
            case "FAILED" -> "❌";
            case "PROCESSING" -> "⏳";
            case "PENDING" -> "⏸️";
            default -> "📬";
        };
    }
    /**
     * Send webhook request (blocking)
     */
    private void sendWebhookSync(Map<String, Object> payload) {
        try {
            slackWebClient.post()
                    .uri(slackConfig.getWebhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(slackConfig.getReadTimeout()))
                    .doOnSuccess(response -> log.debug("Slack webhook sent successfully"))
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("Slack webhook sync failed: {}", e.getMessage());
        }
    }

}
