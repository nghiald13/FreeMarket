package com.ldn.authservice.auth.dto.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotEmpty
        String token,

        @NotEmpty
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character."
        )
        String password
) {
}
