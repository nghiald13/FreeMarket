package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountMfaException extends BusinessException {
    private static final String message = "MFA max retires exceeded!";
    private static final String error = "AccountMfaException";

    public AccountMfaException() { super(message, error); }
    public AccountMfaException(String message) {
        super(message, error);
    }
}
