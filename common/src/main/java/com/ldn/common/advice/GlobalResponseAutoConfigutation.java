package com.ldn.common.advice;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@ConditionalOnClass(ResponseBodyAdvice.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalResponseAutoConfigutation {

    @Bean
    public GlobalResponseWrapper globalResponseWrapper(ObjectMapper objectMapper) {
        return new GlobalResponseWrapper(objectMapper);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}