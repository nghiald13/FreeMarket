package com.ldn.common.rabbitmq;

public interface DomainEvent {
    String routingKey();
}
