package com.bronx.notification.service.impl;

import com.bronx.notification.dto.telegramTemplate.TelegramTemplateRequest;
import com.bronx.notification.dto.telegramTemplate.TelegramTemplateResponse;
import com.bronx.notification.exceptions.ResourceNotFoundException;
import com.bronx.notification.mapper.TelegramTemplateMapper;
import com.bronx.notification.model.entity.TelegramTemplate;
import com.bronx.notification.repository.TelegramTemplateRepository;
import com.bronx.notification.service.TelegramTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramTemplateServiceImpl implements TelegramTemplateService {

    private final TelegramTemplateRepository repository;
    private final TelegramTemplateMapper mapper;

    @Override
    public TelegramTemplateResponse createOrUpdate(TelegramTemplateRequest dto) {
        Optional<TelegramTemplate> existing = repository.findByName(dto.name());
        TelegramTemplate template = existing.orElseGet(TelegramTemplate::new);

        template.setName(dto.name());
        template.setContent(dto.htmlContent());

        return mapper.toResponse(repository.save(template));
    }

    @Override
    public TelegramTemplateResponse getByName(String name) {
        TelegramTemplate template= repository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + name));

        return mapper.toResponse(template);
    }
}
