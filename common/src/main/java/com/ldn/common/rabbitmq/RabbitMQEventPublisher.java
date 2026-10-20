package com.ldn.common.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class RabbitMQEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public <T extends DomainEvent> void publish(T event) {
        this.rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, event.routingKey(), event);
    }

    /** Dùng cho Outbox: gửi JSON có sẵn, chờ broker confirm, ném exception nếu thất bại */
    public void publishRaw(String routingKey, String messageId, String jsonPayload, Map<String, Object> headers) {
        MessageBuilder builder = (MessageBuilder) MessageBuilder
                .withBody(jsonPayload.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setMessageId(messageId)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        headers.forEach(builder::setHeader);
        Message message = builder.build();

        CorrelationData cd = new CorrelationData(messageId);
        rabbitTemplate.send(RabbitMQConstants.EXCHANGE, routingKey, message, cd);
        try {
            CorrelationData.Confirm confirm = cd.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack()) throw new AmqpException("Broker NACK: " + confirm.reason());
            if (cd.getReturned() != null)
                throw new AmqpException("Unroutable: " + cd.getReturned().getReplyText());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpException("Interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new AmqpException("Confirm failed", e);
        }
    }
}
