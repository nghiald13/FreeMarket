package com.ldn.common.rabbitmq;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RabbitMQConstants {
    public static final String EXCHANGE = "app.events";
    public static final String DLX = "app.events.dlx";
}
