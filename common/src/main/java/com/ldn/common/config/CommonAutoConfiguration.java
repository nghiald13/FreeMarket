package com.ldn.common.config;

import com.ldn.common.advice.GlobalExceptionHandler;
import com.ldn.common.advice.GlobalResponseWrapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@ConditionalOnClass(ResponseBodyAdvice.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonAutoConfiguration {

    @Bean
    public GlobalResponseWrapper globalResponseWrapper(ObjectMapper objectMapper) {
        return new GlobalResponseWrapper(objectMapper);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}