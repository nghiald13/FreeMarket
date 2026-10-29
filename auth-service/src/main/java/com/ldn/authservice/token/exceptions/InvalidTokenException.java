package com.ldn.authservice.token.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
    private static final String message = "Token does not exist or expired!";
    private static final String error = "InvalidTokenException";

    public InvalidTokenException() {
        super(message, error);
    }
}
