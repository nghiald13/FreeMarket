package com.ldn.inventoryservice.cart.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidCartItemException extends BusinessException {
    private static final String message = "Invalid Cart Item!";
    private static final String error = "InvalidCartItemException";
    public InvalidCartItemException() { super(message, error); }
    public InvalidCartItemException(String message) {
        super(message, error);
    }
}
