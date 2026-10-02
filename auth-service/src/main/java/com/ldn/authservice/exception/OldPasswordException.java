package com.ldn.authservice.exception;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OldPasswordException extends BusinessException {
    private static final HttpStatus httpStatus = HttpStatus.CONFLICT;
    private static final String message = "Cannot use current password!";
    public OldPasswordException() {super(message, httpStatus);;}
    public OldPasswordException(String message) {
        super(message);
    }
}
