package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ForgotPasswordRequest(
        @NotEmpty
        @Email
        String email
) {
}
