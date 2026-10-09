package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.NotEmpty;

public record VerifyEmailRequest(
        @NotEmpty
        String key
) {
}
