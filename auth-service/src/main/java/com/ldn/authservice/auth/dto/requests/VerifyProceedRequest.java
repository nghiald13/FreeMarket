package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record VerifyProceedRequest(
        @NotEmpty
        String key,

        @Pattern(regexp = "^\\d{6}$")
        String otp
) {
}
