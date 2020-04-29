package com.acmebank.repository.account;

import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.RepositoryException;
import com.acmebank.repository.instance.H2Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class AccountRepository {
    private final static Logger logger = LoggerFactory.getLogger(AccountRepository.class);

    private final H2Connection h2Connection;

    public AccountRepository(final H2Connection h2Connection) {
        this.h2Connection = h2Connection;
    }

    public BigDecimal getBalance(final String accountId) throws AccountNotFoundException, RepositoryException {
        final String query = String.format("SELECT balance FROM account WHERE accountId = '%s'", accountId);

        try (final Connection connection = h2Connection.getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                try (final ResultSet results = statement.executeQuery(query)) {
                    if (results.next()) {
                        return results.getBigDecimal("balance");
                    } else {
                        final String message = String.format("Could not find account '%s'", accountId);
                        logger.warn(message);
                        throw new AccountNotFoundException(message, accountId);
                    }
                }
            }
        } catch (final SQLException e) {
            final String message = "Unable to get account balance";
            logger.error(message, e);
            throw new RepositoryException(message, e);
        }
    }

    public void updateBalance(final List<BalanceUpdate> balanceUpdates) throws RepositoryException {
        final Connection connection = h2Connection.getConnection();
        try {
            try {
                // Apply multiple updates in one transaction
                connection.setAutoCommit(false);
                applyBalanceUpdates(balanceUpdates, connection);
                connection.commit();
            } catch (final SQLException commitException) {
                final String message = "Unable to update account";
                logger.error(message, commitException);
                try {
                    connection.rollback();
                } catch (final SQLException rollbackException) {
                    final String rollbackMessage = "Unable to update account, was unable to rollback change";
                    logger.error(rollbackMessage, rollbackException);
                    throw new RepositoryException(rollbackMessage, rollbackException);
                }
                throw new RepositoryException(message, commitException);
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (final SQLException e) {
            final String message = "Unable to update account, was unable to finish connection";
            logger.error(message, e);
            throw new RepositoryException(message, e);
        }
    }

    private void applyBalanceUpdates(final List<BalanceUpdate> balanceUpdates,
                                     final Connection connection) throws SQLException {
        final String sql = "UPDATE account SET balance = ? WHERE accountId = ?";
        try (final PreparedStatement updateStatement = connection.prepareStatement(sql)) {
            for (final BalanceUpdate balanceUpdate : balanceUpdates) {
                updateStatement.setBigDecimal(1, balanceUpdate.getBalance());
                updateStatement.setString(2, balanceUpdate.getAccountId());
                updateStatement.execute();
            }
        }
    }

}
