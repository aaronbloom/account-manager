package com.acmebank.bank;

import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.AccountOperationException;
import com.acmebank.exception.RepositoryException;
import com.acmebank.repository.account.AccountRepository;
import com.acmebank.repository.account.BalanceUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class BankManager {
    private final static Logger logger = LoggerFactory.getLogger(BankManager.class);
    private final static Object TRANSFER_LOCK = new Object();

    private final AccountRepository accountRepository;

    public BankManager(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BigDecimal getAccountBalance(final String accountId) throws AccountNotFoundException, RepositoryException {
        logger.info("Getting account balance for account '{}'", accountId);
        return accountRepository.getBalance(accountId);
    }

    public void transfer(final String fromAccountId,
                         final String targetAccountId,
                         final BigDecimal amount) throws AccountNotFoundException, RepositoryException, AccountOperationException {
        if (fromAccountId.equals(targetAccountId)) {
            throw new AccountOperationException("Cannot transfer between the same accounts", fromAccountId);
        }

        synchronized (TRANSFER_LOCK) {
            final BigDecimal fromBalance = accountRepository.getBalance(fromAccountId);
            if (fromBalance.compareTo(amount) < 0) {
                throw new AccountOperationException("", fromAccountId);
            }
            final BigDecimal targetBalance = accountRepository.getBalance(targetAccountId);

            final BigDecimal newFromBalance = fromBalance.subtract(amount);
            final BigDecimal newTargetBalance = targetBalance.add(amount);

            final List<BalanceUpdate> balanceUpdates = Arrays.asList(
                    new BalanceUpdate(fromAccountId, newFromBalance),
                    new BalanceUpdate(targetAccountId, newTargetBalance)
            );

            accountRepository.updateBalance(balanceUpdates);
        }
    }

}
