package com.acmebank.exception;

public class AccountOperationException extends Exception {
    public AccountOperationException(final String message) {
        super(message);
    }
}
