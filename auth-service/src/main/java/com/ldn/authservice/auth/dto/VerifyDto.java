package com.ldn.authservice.auth.dto;

import lombok.Builder;

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
