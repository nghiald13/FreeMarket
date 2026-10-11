package com.ldn.financeservice.rabbitmq;

import com.ldn.common.rabbitmq.RabbitMQConstants;
import com.ldn.common.rabbitmq.RabbitMQEvents;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AUTH_QUEUE = "finance.auth.queue";

    @Bean
    public Queue financeAuthQueue() {
        return QueueBuilder.durable(AUTH_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX)
                .withArgument("x-dead-letter-routing-key", AUTH_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Binding financeAuthBinding(
            @Qualifier("financeAuthQueue")
            Queue financeAuthQuue,
            TopicExchange appExchange
    ) {
        return BindingBuilder.bind(financeAuthQuue)
                .to(appExchange)
                .with(RabbitMQEvents.ACCOUNT_CREATED);
    }

    @Bean
    public Queue financeAuthDlq() {
        return QueueBuilder.durable(AUTH_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding financeAuthDlqBinding(
            @Qualifier("financeAuthDlq")
            Queue financeAuthDlq,
            TopicExchange deadLetterExchange
    ) {
        return BindingBuilder.bind(financeAuthDlq)
                .to(deadLetterExchange)
                .with(AUTH_QUEUE + ".dlq");
    }
}

