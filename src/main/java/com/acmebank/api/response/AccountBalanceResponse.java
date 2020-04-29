package com.acmebank.api.response;

import java.math.BigDecimal;

public class AccountBalanceResponse {
    private final String accountId;
    private final BigDecimal balance;

    public AccountBalanceResponse(final String accountId, final BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
