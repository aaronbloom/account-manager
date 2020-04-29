package com.acmebank.exception;

public class RepositoryException extends Exception {
    public RepositoryException(final String message, final Exception e) {
        super(message, e);
    }
    public RepositoryException(final String message) {
        super(message);
    }
}
