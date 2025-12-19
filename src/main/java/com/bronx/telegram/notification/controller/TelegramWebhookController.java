package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.model.enumz.BotStatus;
import com.bronx.telegram.notification.repository.TelegramBotRepository;
import com.bronx.telegram.notification.service.WebhookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook/telegram")
@Slf4j
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final WebhookService webhookService;
    private final TelegramBotRepository botRepository;

    @PostMapping("/{botId}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable Long botId,
            @RequestBody String payload) {

        log.debug("Received webhook for bot {}", botId);

        try {
            // Verify bot exists and is active
            TelegramBot bot = botRepository.findById(botId)
                    .orElseThrow(() -> new EntityNotFoundException("Bot not found"));

            if (bot.getStatus() != BotStatus.ACTIVE) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Bot is not active");
            }

            // Parse and process webhook
            webhookService.processWebhook(bot, payload);

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Failed to process webhook for bot {}", botId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Processing failed");
        }
    }
}

