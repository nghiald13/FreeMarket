package com.ldn.common.rabbitmq.events;

import com.ldn.common.rabbitmq.DomainEvent;
import com.ldn.common.rabbitmq.RabbitMQEvents;
import lombok.Builder;

@Builder
public record PaymentSuccessEvent(
        Long eventId,
        String eventType,
        String payload,
        String provider
) implements DomainEvent {
    @Override
    public String routingKey() {
        return RabbitMQEvents.PAYMENT_SUCCESS;
    }
}
