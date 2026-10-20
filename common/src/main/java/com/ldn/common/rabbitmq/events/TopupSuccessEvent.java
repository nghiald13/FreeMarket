package com.ldn.common.rabbitmq.events;

import com.ldn.common.rabbitmq.DomainEvent;
import com.ldn.common.rabbitmq.RabbitMQEvents;
import lombok.Builder;

@Builder
public record TopupSuccessEvent(
        Long eventId,
        String eventType,
        String providerRefId,
        String providerTxnId,
        Long amount

) implements DomainEvent {
    @Override
    public String routingKey() {
        return RabbitMQEvents.TOPUP_SUCCESS;
    }
}
