package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountInactivedException extends BusinessException {
    private static final String message = "This account has not been verified!";

    public AccountInactivedException() {
        super(message);
    }

    public AccountInactivedException(String message) {
        super(message);
    }
}
