package com.skybus.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology:
 *
 * Exchange: skybus.booking.exchange (topic)
 *   └── routing key "booking.created"   → skybus.booking.created.queue
 *   └── routing key "booking.cancelled" → skybus.booking.cancelled.queue
 *
 * Notification-service subscribes to both queues to send confirmation emails.
 * Other services (analytics, etc.) can bind their own queues to the same exchange.
 *
 * Durable queues + durable exchange = messages survive RabbitMQ restarts.
 */
@Configuration
public class RabbitConfig {

    // Constants
    public static final String EXCHANGE               = "skybus.booking.exchange";
    public static final String QUEUE_BOOKING_CREATED  = "skybus.booking.created.queue";
    public static final String QUEUE_BOOKING_CANCELLED = "skybus.booking.cancelled.queue";
    public static final String ROUTING_CREATED        = "booking.created";
    public static final String ROUTING_CANCELLED      = "booking.cancelled";

    // Exchange
    @Bean
    public TopicExchange bookingExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    // Queues
    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CREATED).build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CANCELLED).build();
    }

    // Bindings
    @Bean
    public Binding bindingCreated() {
        return BindingBuilder
                .bind(bookingCreatedQueue())
                .to(bookingExchange())
                .with(ROUTING_CREATED);
    }

    @Bean
    public Binding bindingCancelled() {
        return BindingBuilder
                .bind(bookingCancelledQueue())
                .to(bookingExchange())
                .with(ROUTING_CANCELLED);
    }

    // JSON message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}