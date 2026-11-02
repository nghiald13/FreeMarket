package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OldPasswordException extends BusinessException {
    private static final HttpStatus httpStatus = HttpStatus.CONFLICT;
    private static final String message = "Cannot use current password!";
    private static final String error = "OldPasswordException";
    public OldPasswordException() {super(message, httpStatus, error);;}
    public OldPasswordException(String message) {
        super(message, error);
    }
}
