package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.telegrambot.BotRequest;
import com.bronx.telegram.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.telegram.notification.service.BotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram_bots")
@Slf4j
@RequiredArgsConstructor
public class TelegramBotController {

    private final BotService botService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TelegramBotResponse> createBot(
            @Valid @RequestBody BotRequest request
    ) {
        TelegramBotResponse response = botService.createNewBot(request);
        return ApiResponse.success("TelegramBot created successfully", response);
    }

}
