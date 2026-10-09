package com.ldn.authservice.auth.exceptions;

import com.ldn.common.exception.BusinessException;

public class AccountSuspendedException extends BusinessException {
    private static final String message = "Account has been suspended!";

    public AccountSuspendedException() {
        super(message);
    }

    public AccountSuspendedException(String message) {
        super(message);
    }
}
