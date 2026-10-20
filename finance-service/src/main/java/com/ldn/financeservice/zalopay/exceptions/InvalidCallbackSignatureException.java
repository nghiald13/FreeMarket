package com.ldn.financeservice.zalopay.exceptions;

import com.ldn.common.exception.BusinessException;

public class InvalidCallbackSignatureException extends BusinessException {
    private static final String message = "Invalid callback signature!";
    public InvalidCallbackSignatureException() {super(message);}
    public InvalidCallbackSignatureException(String message) {
        super(message);
    }
}
