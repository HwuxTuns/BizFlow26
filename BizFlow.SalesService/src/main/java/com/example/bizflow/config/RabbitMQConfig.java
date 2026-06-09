package com.example.bizflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ==================== QUEUE NAMES ====================
    public static final String ORDER_QUEUE = "bizflow.order.queue";
    public static final String NOTIFICATION_QUEUE = "bizflow.notification.queue";
    public static final String INVENTORY_QUEUE = "bizflow.inventory.queue";

    // ==================== EXCHANGE NAMES ====================
    public static final String ORDER_EXCHANGE = "bizflow.order.exchange";
    
    // ==================== ROUTING KEYS ====================
    public static final String ORDER_ROUTING_KEY = "order.created";
    public static final String NOTIFICATION_ROUTING_KEY = "order.notification";
    public static final String INVENTORY_ROUTING_KEY = "order.inventory";

    // ==================== QUEUE DECLARATIONS ====================
    
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true); // durable = true
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE, true);
    }

    // ==================== EXCHANGE DECLARATION ====================
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // ==================== BINDINGS ====================
    
    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderQueue)
                .to(orderExchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(orderExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(inventoryQueue)
                .to(orderExchange)
                .with(INVENTORY_ROUTING_KEY);
    }

    // ==================== MESSAGE CONVERTER ====================
    
    @Bean
    public MessageConverter messageConverter() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @SuppressWarnings("null")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
