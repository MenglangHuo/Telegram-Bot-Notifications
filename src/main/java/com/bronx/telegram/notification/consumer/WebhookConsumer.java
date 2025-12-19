package com.bronx.telegram.notification.consumer;
import com.bronx.telegram.notification.configs.RabbitMqConfig;
import com.bronx.telegram.notification.dto.webhook.WebhookMessage;
import com.bronx.telegram.notification.model.entity.Webhook;
import com.bronx.telegram.notification.repository.WebhookRepository;
import com.bronx.telegram.notification.service.WebhookCommandService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookConsumer {

    private final WebhookRepository webhookRepository;
    private final WebhookCommandService webhookCommandService;

    @RabbitListener(
            queues = RabbitMqConfig.WEBHOOK_PROCESSING_QUEUE,
            concurrency = "5-15",
            ackMode = "AUTO"
    )
    public void processWebhook(WebhookMessage message) {
        log.info("Processing webhook {} with command {}",
                message.getWebhookId(), message.getCommand());

        try {
            Webhook webhook = webhookRepository.findById(message.getWebhookId())
                    .orElseThrow(() -> new EntityNotFoundException("Webhook not found"));

            // Process based on command type
            if (message.getCommand() != null) {
                webhookCommandService.handleCommand(webhook, message);
            }

            webhook.setProcessed(true);
            webhook.setProcessedAt(Instant.now());
            webhookRepository.save(webhook);

            log.info("Successfully processed webhook {}", message.getWebhookId());

        } catch (Exception e) {
            log.error("Error processing webhook {}", message.getWebhookId(), e);
            Webhook webhook = webhookRepository.findById(message.getWebhookId())
                    .orElse(null);
            if (webhook != null) {
                webhook.setProcessingError(e.getMessage());
                webhook.setRetryCount(webhook.getRetryCount() + 1);
                webhookRepository.save(webhook);
            }
        }
    }
}
