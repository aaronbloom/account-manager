package com.acmebank.accountmanager.exception;

public class ApplicationStartupException extends Exception {
    public ApplicationStartupException(final String message, final Throwable e) {
        super(message, e);
    }
}
