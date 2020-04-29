package com.acmebank.exception;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
