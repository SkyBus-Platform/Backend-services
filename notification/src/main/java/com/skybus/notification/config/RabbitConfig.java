package com.skybus.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * RabbitMQ configuration for notification-service.
 */
@Configuration
public class RabbitConfig {

    // Queue and routing key names
    public static final String EXCHANGE                  = "skybus.booking.exchange";
    public static final String QUEUE_BOOKING_CREATED     = "skybus.booking.created.queue";
    public static final String QUEUE_BOOKING_CANCELLED   = "skybus.booking.cancelled.queue";

    private static final String DLQ_EXCHANGE             = "skybus.dlq.exchange";
    private static final String DLQ_BOOKING_CREATED      = "skybus.booking.created.dlq";
    private static final String DLQ_BOOKING_CANCELLED    = "skybus.booking.cancelled.dlq";
    private static final String DLQ_ROUTING_CREATED      = "dlq.booking.created";
    private static final String DLQ_ROUTING_CANCELLED    = "dlq.booking.cancelled";

    //Main exchange
    @Bean
    public TopicExchange bookingExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    //Dead-letter exchange
    @Bean
    public DirectExchange dlqExchange() {
        return ExchangeBuilder.directExchange(DLQ_EXCHANGE).durable(true).build();
    }

    //Main queues (with DLQ routing configured)
    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CREATED)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_CREATED)
                .build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_CANCELLED)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_CANCELLED)
                .build();
    }

    // Dead-letter queues
    @Bean
    public Queue bookingCreatedDlq() {
        return QueueBuilder.durable(DLQ_BOOKING_CREATED).build();
    }

    @Bean
    public Queue bookingCancelledDlq() {
        return QueueBuilder.durable(DLQ_BOOKING_CANCELLED).build();
    }

    // Bindings
    @Bean
    public Binding createdBinding() {
        return BindingBuilder.bind(bookingCreatedQueue())
                .to(bookingExchange()).with("booking.created");
    }

    @Bean
    public Binding cancelledBinding() {
        return BindingBuilder.bind(bookingCancelledQueue())
                .to(bookingExchange()).with("booking.cancelled");
    }

    @Bean
    public Binding createdDlqBinding() {
        return BindingBuilder.bind(bookingCreatedDlq())
                .to(dlqExchange()).with(DLQ_ROUTING_CREATED);
    }

    @Bean
    public Binding cancelledDlqBinding() {
        return BindingBuilder.bind(bookingCancelledDlq())
                .to(dlqExchange()).with(DLQ_ROUTING_CANCELLED);
    }

    // Message converter
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

    // Retry interceptor
    /**
     * Applied to @RabbitListener methods via the container factory below.
     *
     * Attempts: 3
     * Backoff:  1s → 2s → 4s (multiplier 2.0, max 10s)
     * Recovery: RejectAndDontRequeueRecoverer — after 3 failures, rejects the
     *           message so RabbitMQ routes it to the DLQ via x-dead-letter-exchange.
     */
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    /**
     * Container factory that applies the retry interceptor to all
     * @RabbitListener methods in this service.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAdviceChain(retryInterceptor());
        factory.setDefaultRequeueRejected(false); // don't re-queue on rejection
        return factory;
    }
}