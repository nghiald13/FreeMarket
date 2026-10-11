package com.ldn.common.rabbitmq;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RabbitMQAutoConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange appExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQConstants.DLX).durable(true).build();
    }

    @Bean
    public RabbitMQEventPublisher rabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        return new RabbitMQEventPublisher(rabbitTemplate);
    }
}
