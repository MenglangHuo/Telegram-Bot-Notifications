package com.bronx.telegram.notification.controller;

import com.bronx.telegram.notification.dto.baseResponse.ApiResponse;
import com.bronx.telegram.notification.dto.baseResponse.PageResponse;
import com.bronx.telegram.notification.dto.division.DivisionRequest;
import com.bronx.telegram.notification.dto.division.DivisionResponse;
import com.bronx.telegram.notification.dto.partner.PartnerResponse;
import com.bronx.telegram.notification.dto.telegrambot.BotRequest;
import com.bronx.telegram.notification.dto.telegrambot.TelegramBotResponse;
import com.bronx.telegram.notification.model.entity.TelegramBot;
import com.bronx.telegram.notification.service.BotService;
import com.bronx.telegram.notification.service.DivisionService;
import com.bronx.telegram.notification.service.TelegramBotService;
import com.bronx.telegram.notification.service.impl.BotServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
