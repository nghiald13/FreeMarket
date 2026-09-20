package com.ldn.common.advice;

import com.ldn.common.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        int statusCode = extractStatusCode(response);

        if (body instanceof String) {
            return toJsonString(ApiResponse.success(body, "Success", statusCode));
        }

        return ApiResponse.success(body, "Success", statusCode);
    }

    private int extractStatusCode(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            return servletResponse.getServletResponse().getStatus();
        }
        return 200;
    }

    private String toJsonString(ApiResponse<?> response) {
        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
