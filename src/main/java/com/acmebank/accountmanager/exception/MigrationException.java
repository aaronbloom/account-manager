package com.acmebank.accountmanager.exception;

public class MigrationException extends Exception {
    public MigrationException(final String message, final Throwable e) {
        super(message, e);
    }
}
