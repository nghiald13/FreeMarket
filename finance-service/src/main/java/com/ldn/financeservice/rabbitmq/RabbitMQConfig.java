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
    public static final String PAYMENT_SUCCESS_QUEUE = "payment.success";
    public static final String TOPUP_SUCCESS_QUEUE = "topup.success";

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

    @Bean
    public Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_SUCCESS_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Binding paymentSuccessBinding(@Qualifier("paymentSuccessQueue") Queue queue, TopicExchange appExchange) {
        return BindingBuilder.bind(queue).to(appExchange).with("payment.success");
    }

    @Bean
    public Queue paymentSuccessDlq() {
        return QueueBuilder.durable(PAYMENT_SUCCESS_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding paymentSuccessDlqBinding(@Qualifier("paymentSuccessDlq") Queue dlq,
                                            TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(dlq).to(deadLetterExchange).with(PAYMENT_SUCCESS_QUEUE + ".dlq");
    }

    @Bean
    public Queue topupSuccessQueue() {
        return QueueBuilder.durable(TOPUP_SUCCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.DLX)
                .withArgument("x-dead-letter-routing-key", TOPUP_SUCCESS_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Binding topupSuccessBinding(@Qualifier("topupSuccessQueue") Queue queue, TopicExchange appExchange) {
        return BindingBuilder.bind(queue).to(appExchange).with(TOPUP_SUCCESS_QUEUE);
    }

    @Bean
    public Queue topupSuccessDlq() {
        return QueueBuilder.durable(TOPUP_SUCCESS_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding topupSuccessDlqBinding(@Qualifier("topupSuccessDlq") Queue dlq,
                                            TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(dlq).to(deadLetterExchange).with(TOPUP_SUCCESS_QUEUE + ".dlq");
    }
}

