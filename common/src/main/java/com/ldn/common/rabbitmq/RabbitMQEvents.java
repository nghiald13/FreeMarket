package com.ldn.common.rabbitmq;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class RabbitMQEvents {
    public static final String ACCOUNT_CREATED = "account.created";
    public static final String PAYMENT_SUCCESS = "payment.success";
    public static final String TOPUP_SUCCESS = "topup.success";
}
