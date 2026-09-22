package com.ldn.authservice.dto.request;

public record RegisterRequest(
        String name,
        String email,
        String password,
        String phone
) {
}
