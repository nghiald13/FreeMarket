package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {
    private static final String message = "Invalid credentials!";
    private static final String error = "InvalidCredentialsException";

    public InvalidCredentialsException() {
        super(message, error);
    }
}
