package com.acmebank.api;

import com.acmebank.bank.BankManager;
import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.AccountOperationException;
import com.acmebank.exception.RepositoryException;

import java.math.BigDecimal;

public class AccountController {

    private final BankManager bankManager;

    public AccountController(final BankManager bankManager) {
        this.bankManager = bankManager;
    }

    public String getBalance(final String accountId) {
        final BigDecimal balance;
        try {
            balance = bankManager.getAccountBalance(accountId);
        } catch (final AccountNotFoundException e) {
            e.printStackTrace();
            return "404"; // TODO - 404
        } catch (final RepositoryException e) {
            e.printStackTrace();
            return "500"; // TODO - 500
        }
        return String.format("{ \"accountId\":\"%s\", \"balance\":\"%s\" }", accountId, balance.toString());
    }

    public String transfer(final String fromAccountId, final String targetAccountId, final String amountString) {
        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountString);
        } catch (final NumberFormatException e) {
            // TODO - 404
            return "404";
        }
        try {
            bankManager.transfer(fromAccountId, targetAccountId, amount);
        } catch (final AccountNotFoundException e) {
            e.printStackTrace();
            return "404"; // TODO - 404
        } catch (final RepositoryException e) {
            e.printStackTrace();
            return "500"; // TODO - 500
        } catch (final AccountOperationException e) {
            e.printStackTrace();
            return "400"; // TODO - 400
        }
        return String.format("{ \"fromAccountId\":\"%s\", \"targetAccountId\":\"%s\", \"transferAmount\":\"%s\" }",
                fromAccountId, targetAccountId, amount);
    }

}
