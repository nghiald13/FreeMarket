package com.ldn.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record VerifyEmailRequest(
        @NotEmpty
        String key
) {
}
