package com.bronx.notification.repository;

import com.bronx.notification.model.entity.TelegramTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramTemplateRepository extends JpaRepository<TelegramTemplate, Long> {

    Optional<TelegramTemplate> findByName(String name);
}
