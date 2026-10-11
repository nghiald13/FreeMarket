package com.ldn.common.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public <T extends DomainEvent> void publish(T event) {
        this.rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, event.routingKey(), event);
    }
}
