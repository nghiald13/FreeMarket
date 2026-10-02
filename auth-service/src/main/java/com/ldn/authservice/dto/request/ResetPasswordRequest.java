package com.ldn.authservice.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record ResetPasswordRequest(
        @NotEmpty
        String token,

        @NotEmpty
        String password
) {
}
