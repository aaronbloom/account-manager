package com.acmebank.exception;

public class AccountOperationException extends Exception {
    private final String accountId;

    public AccountOperationException(final String message, final String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
