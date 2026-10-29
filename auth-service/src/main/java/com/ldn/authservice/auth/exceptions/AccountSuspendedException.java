package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountSuspendedException extends BusinessException {
    private static final String message = "Account has been suspended!";
    private static final String error = "AccountSuspendedException";

    public AccountSuspendedException() {
        super(message, error);
    }

    public AccountSuspendedException(String message) {
        super(message, error);
    }
}
