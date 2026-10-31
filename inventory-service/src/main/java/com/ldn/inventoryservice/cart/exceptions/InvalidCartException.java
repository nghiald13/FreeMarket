package com.ldn.inventoryservice.cart.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidCartException extends BusinessException {
    private static final String message = "Invalid cart!";
    private static final String error = "InvalidCartException";

    public InvalidCartException() {
        super(message, error);
    }

    public InvalidCartException(String message) {
        super(message, error);
    }
}
