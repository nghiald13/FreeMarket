package com.ldn.financeservice.zalopay.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zalopay")
public record ZaloPayProperties(
        String baseUrl,
        int appId,
        String key1,
        String key2,
        String publicKey,
        String privateKey,
        String webhook
) {
}
