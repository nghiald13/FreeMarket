package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountInactivedException extends BusinessException {
    private static final String message = "This account has not been verified!";
    private static final String error = "AccountInactiveException";

    public AccountInactivedException() {
        super(message, error);
    }

    public AccountInactivedException(String message) {
        super(message, error);
    }
}
