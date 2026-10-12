package com.ldn.financeservice.zalopay.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class ZaloPayRestClientConfig {
    private final ZaloPayProperties zaloPayProperties;

    @Bean
    public RestClient zaloPayClient() {
        return RestClient.builder()
                .baseUrl(zaloPayProperties.baseUrl())
                .build();
    }
}
