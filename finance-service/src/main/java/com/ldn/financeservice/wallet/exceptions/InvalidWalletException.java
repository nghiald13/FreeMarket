package com.ldn.financeservice.wallet.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidWalletException extends BusinessException {
    private static final String message = "Invalid Wallet!";

    public InvalidWalletException() {
        super(message);
    }

    public InvalidWalletException(String message) {
        super(message);
    }
}
