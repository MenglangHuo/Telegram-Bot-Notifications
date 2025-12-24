package com.bronx.notification.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    // Queue names
    public static final String NOTIFICATION_PERSONAL_QUEUE = "notification.personal.queue";
    public static final String NOTIFICATION_CHANNEL_QUEUE = "notification.channel.queue";
    public static final String WEBHOOK_PROCESSING_QUEUE = "webhook.processing.queue";
    public static final String NOTIFICATION_RETRY_QUEUE = "notification.retry.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String WEBHOOK_EXCHANGE = "webhook.exchange";
    public static final String RETRY_EXCHANGE = "retry.exchange";

    // Routing keys
    public static final String PERSONAL_ROUTING_KEY = "notification.personal";
    public static final String CHANNEL_ROUTING_KEY = "notification.channel";
    public static final String WEBHOOK_ROUTING_KEY = "webhook.process";
    public static final String RETRY_ROUTING_KEY = "notification.retry";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ==================== Notification Queues ====================

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue personalNotificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_PERSONAL_QUEUE)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue channelNotificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_CHANNEL_QUEUE)
                .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding personalNotificationBinding(Queue personalNotificationQueue,
                                               DirectExchange notificationExchange) {
        return BindingBuilder.bind(personalNotificationQueue)
                .to(notificationExchange)
                .with(PERSONAL_ROUTING_KEY);
    }

    @Bean
    public Binding channelNotificationBinding(Queue channelNotificationQueue,
                                              DirectExchange notificationExchange) {
        return BindingBuilder.bind(channelNotificationQueue)
                .to(notificationExchange)
                .with(CHANNEL_ROUTING_KEY);
    }

    // ==================== Webhook Queue ====================

    @Bean
    public DirectExchange webhookExchange() {
        return new DirectExchange(WEBHOOK_EXCHANGE, true, false);
    }

    @Bean
    public Queue webhookProcessingQueue() {
        return QueueBuilder.durable(WEBHOOK_PROCESSING_QUEUE)
                .build();
    }

    @Bean
    public Binding webhookBinding(Queue webhookProcessingQueue,
                                  DirectExchange webhookExchange) {
        return BindingBuilder.bind(webhookProcessingQueue)
                .to(webhookExchange)
                .with(WEBHOOK_ROUTING_KEY);
    }

    // ==================== Retry & DLQ ====================

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE, true, false);
    }

    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(NOTIFICATION_RETRY_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5 minutes
                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange retryExchange) {
        return BindingBuilder.bind(retryQueue)
                .to(retryExchange)
                .with(RETRY_ROUTING_KEY);
    }
}
