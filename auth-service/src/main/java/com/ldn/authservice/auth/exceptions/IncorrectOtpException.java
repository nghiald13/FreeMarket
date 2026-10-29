package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class IncorrectOtpException extends BusinessException {
    private static final String error = "IncorrectOtpException";
    private static final String message = "Incorrect Otp!";
    public IncorrectOtpException() {super(message, error);}
}
