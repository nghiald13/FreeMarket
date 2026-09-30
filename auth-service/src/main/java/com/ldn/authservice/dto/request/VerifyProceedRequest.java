package com.ldn.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record VerifyProceedRequest(
        @NotEmpty
        String key,

        @Pattern(regexp = "^\\d{6}$")
        String otp,

        @Email
        String email
) {
}
