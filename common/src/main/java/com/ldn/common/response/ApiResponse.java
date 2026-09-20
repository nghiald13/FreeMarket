package com.ldn.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int statusCode;
    private String message;
    private T data;
    private Object error;

    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success", 200);
    }

    public static ApiResponse<?> error(String message, Object error, int statusCode) {
        return ApiResponse.builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .error(error)
                .build();
    }
}
