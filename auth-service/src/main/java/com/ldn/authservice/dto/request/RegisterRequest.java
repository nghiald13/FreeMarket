package com.ldn.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "name cannot be blank")
        @Size(min = 2, max = 40, message = "name must contain from 2 to 40 characters")
        String name,

        @NotBlank(message = "email cannot be blank")
        @Email(message = "email must have correct format")
        String email,

        @NotBlank(message = "password cannot be blank")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, number, and special character."
        )
        String password,

        @NotBlank
        @Pattern(regexp = "^0\\d{9}$")
        String phone
) {
}
