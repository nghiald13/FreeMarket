package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String emailOrPhone,
        @NotBlank String password
) {}