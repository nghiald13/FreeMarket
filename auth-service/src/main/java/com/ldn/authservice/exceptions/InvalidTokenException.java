package com.ldn.authservice.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
    private static final String message = "Token does not exist or expired!";

    public InvalidTokenException() {
        super(message);
    }
}
