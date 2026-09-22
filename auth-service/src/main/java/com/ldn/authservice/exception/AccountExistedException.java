package com.ldn.authservice.exception;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AccountExistedException extends BusinessException {
    private static final HttpStatus httpStatus = HttpStatus.CONFLICT;
    private static final String message = "Email or Phone have already been registered!";

    public AccountExistedException() {
        super(message, httpStatus);
    }
    public AccountExistedException(String message) {
        super(message, httpStatus);
    }
}
