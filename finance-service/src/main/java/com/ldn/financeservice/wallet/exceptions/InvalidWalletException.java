package com.ldn.financeservice.wallet.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidWalletException extends BusinessException {
    private static final String message = "Invalid Wallet!";
    private static final String error = "InvalidWalletException";

    public InvalidWalletException() {
        super(message, error);
    }

    public InvalidWalletException(String message) {
        super(message, error);
    }
}
