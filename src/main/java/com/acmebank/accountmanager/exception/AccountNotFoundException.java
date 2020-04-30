package com.acmebank.accountmanager.exception;

public class AccountNotFoundException extends Exception {
    private final String accountId;

    public AccountNotFoundException(final String message, final String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
