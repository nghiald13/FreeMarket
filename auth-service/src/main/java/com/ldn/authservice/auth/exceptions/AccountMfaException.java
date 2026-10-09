package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountMfaException extends BusinessException {
    private static final String message = "MFA max retires exceeded!";

    public AccountMfaException() { super(message); }
    public AccountMfaException(String message) {
        super(message);
    }
}
