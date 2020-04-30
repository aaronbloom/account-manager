package com.acmebank.api.controller;

import com.acmebank.api.response.AccountBalanceResponse;
import com.acmebank.bank.BankManager;
import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.AccountOperationException;
import com.acmebank.exception.RepositoryException;
import io.javalin.http.Context;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class AccountController {
    private final static Logger logger = LoggerFactory.getLogger(BankManager.class);
    private final BankManager bankManager;

    public AccountController(final BankManager bankManager) {
        this.bankManager = bankManager;
    }

    public void getBalance(final Context ctx) {
        final String accountId = ctx.pathParam("accountId", String.class)
                .check(x -> !StringUtil.isEmpty(x), "accountId parameter cannot be missing")
                .get();

        try {
            final BigDecimal balance = bankManager.getAccountBalance(accountId);
            ctx.json(new AccountBalanceResponse(accountId, balance));
        } catch (final AccountNotFoundException e) {
            logger.warn("Unable to handle request", e);
            ctx.status(404).result(String.format("Account not found '%s'", e.getAccountId()));
        } catch (final RepositoryException e) {
            logger.warn("Unable to handle request", e);
            ctx.status(500).result("Unable to facilitate request");
        }
    }

    public void transfer(final Context ctx) {
        final String accountId = ctx.pathParam("accountId", String.class)
                .check(x -> !StringUtil.isEmpty(x), "accountId parameter cannot be missing")
                .get();
        final String targetAccountId = ctx.pathParam("targetAccountId", String.class)
                .check(x -> !StringUtil.isEmpty(x), "targetAccountId parameter cannot be missing")
                .check(x -> !accountId.equals(x), "accountId and targetAccountId cannot be the same")
                .get();
        final BigDecimal amount = ctx.pathParam("amount", BigDecimal.class)
                .check(x -> x.compareTo(BigDecimal.ZERO) > 0, "amount parameter must be greater than zero")
                .get();

        try {
            bankManager.transfer(accountId, targetAccountId, amount);
            ctx.status(200);
        } catch (final AccountNotFoundException e) {
            logger.warn("Unable to handle request", e);
            ctx.status(404).result(String.format("Account not found '%s'", e.getAccountId()));
        } catch (final RepositoryException e) {
            logger.warn("Unable to handle request", e);
            ctx.status(500).result("Unable to facilitate request");
        } catch (final AccountOperationException e) {
            logger.warn("Unable to handle request", e);
            ctx.status(400).result("Invalid request state");
        }
    }

}
