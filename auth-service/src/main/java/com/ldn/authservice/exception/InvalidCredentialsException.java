package com.ldn.authservice.exception;

import com.ldn.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {
    private static final String message = "Invalid email/password!";

    public InvalidCredentialsException() {
        super(message);
    }
}
