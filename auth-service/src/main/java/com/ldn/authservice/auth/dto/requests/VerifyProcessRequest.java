package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.Email;

public record VerifyProcessRequest(
        @Email
        String email
) {
}
