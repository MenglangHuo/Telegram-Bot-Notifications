package com.bronx.notification.controller;

import com.bronx.notification.dto.baseResponse.ApiResponse;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;
import com.bronx.notification.service.TelegramTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Slf4j
public class TemplateController {
    private final TelegramTemplateService templateService;

    @PostMapping
    public ApiResponse<TelegramTemplateResponse> create(@RequestBody TelegramTemplateRequest request) {
        return ApiResponse.success("Template created successfully", templateService.createOrUpdate(request));

    }

    @GetMapping("/{name}")
    public ApiResponse<TelegramTemplateResponse> getByName(@PathVariable String name) {
        return ApiResponse.success(templateService.getByName(name));
    }
}