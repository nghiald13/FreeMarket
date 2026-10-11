package com.ldn.common.rabbitmq.events;

import com.ldn.common.rabbitmq.DomainEvent;
import com.ldn.common.rabbitmq.RabbitMQEvents;
import lombok.Builder;

@Builder
public record AccountCreatedEvent(
        Long accountId,
        String email,
        String name
) implements DomainEvent {
    @Override
    public String routingKey() {
        return RabbitMQEvents.ACCOUNT_CREATED;
    }
}
