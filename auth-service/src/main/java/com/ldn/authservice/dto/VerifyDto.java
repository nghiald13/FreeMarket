package com.ldn.authservice.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record VerifyDto(
        String otp,
        int currentAttempt,
        int maxAttempt
) {
    public VerifyDto(String otp) {
        this(otp, 0, 5);
    }
}
