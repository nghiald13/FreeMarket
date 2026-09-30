package com.ldn.authservice.dto.request;

import jakarta.validation.constraints.Email;

public record VerifyProcessRequest(
        @Email
        String email
) {
}
