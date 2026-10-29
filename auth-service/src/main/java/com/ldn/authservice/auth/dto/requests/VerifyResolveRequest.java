package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record VerifyResolveRequest(
        @NotBlank
        String key
) {
}
