package com.acmebank.accountmanager.repository.account;

import java.math.BigDecimal;

public class BalanceUpdate {
    private final String accountId;
    private final BigDecimal balance;

    public BalanceUpdate(final String accountId, final BigDecimal balance) {
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
