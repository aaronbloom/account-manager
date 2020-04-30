package com.acmebank.repository.account;

import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.RepositoryException;
import com.acmebank.repository.instance.H2Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountRepositoryTest {

    private H2Connection h2Connection;
    private Connection connection;
    private Statement statement;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() throws RepositoryException, SQLException {
        h2Connection = mock(H2Connection.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);

        when(h2Connection.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        accountRepository = new AccountRepository(h2Connection);
    }

    @Test
    void shouldGetAccountBalance() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal(eq("balance"))).thenReturn(new BigDecimal(10));

        final BigDecimal balance = accountRepository.getBalance("123");

        assertEquals(new BigDecimal(10), balance, "Should get correct balance");
    }

    @Test
    void shouldNotGetAccountBalanceWhenDoesNotExist() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThrows(AccountNotFoundException.class, () -> accountRepository.getBalance("123"));
    }

    @Test
    void shouldGetAccountBalanceWithRepositoryExceptionWhenSqlExceptionCaught() throws Exception {
        when(statement.executeQuery(anyString())).thenThrow(new SQLException());

        assertThrows(RepositoryException.class, () -> accountRepository.getBalance("123"));
    }

    @Test
    void shouldUpdateBalanceWithNoUpdates() throws RepositoryException {
        accountRepository.updateBalance(Collections.emptyList());

        verifyNoMoreInteractions(h2Connection);
        verifyNoMoreInteractions(connection);
        verifyNoMoreInteractions(statement);
    }

    @Test
    void shouldUpdateBalanceWithOneUpdate() throws Exception {
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("UPDATE account SET balance = ? WHERE accountId = ?"))
                .thenReturn(preparedStatement);

        accountRepository.updateBalance(Collections.singletonList(
                new BalanceUpdate("12345", BigDecimal.valueOf(12))));

        verify(preparedStatement, times(1)).setBigDecimal(eq(1), eq(BigDecimal.valueOf(12)));
        verify(preparedStatement, times(1)).setString(eq(2), eq("12345"));
        verify(preparedStatement, times(1)).execute();

        verify(connection, never()).rollback();
    }

    @Test
    void shouldUpdateBalanceWithMultipleUpdates() throws Exception {
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("UPDATE account SET balance = ? WHERE accountId = ?"))
                .thenReturn(preparedStatement);

        accountRepository.updateBalance(Arrays.asList(
                new BalanceUpdate("12345", BigDecimal.valueOf(12)),
                new BalanceUpdate("77777", BigDecimal.valueOf(42))));

        verify(preparedStatement, times(1)).setBigDecimal(eq(1), eq(BigDecimal.valueOf(12)));
        verify(preparedStatement, times(1)).setString(eq(2), eq("12345"));

        verify(preparedStatement, times(1)).setBigDecimal(eq(1), eq(BigDecimal.valueOf(42)));
        verify(preparedStatement, times(1)).setString(eq(2), eq("77777"));

        verify(preparedStatement, times(2)).execute();

        verify(connection, never()).rollback();
    }

    @Test
    void shouldUpdateBalanceWithRollback() throws Exception {
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        doThrow(new SQLException("Message")).when(connection).commit();

        final List<BalanceUpdate> balanceUpdates = Collections.singletonList(
                new BalanceUpdate("12345", BigDecimal.valueOf(12)));

        assertThrows(RepositoryException.class, () -> accountRepository.updateBalance(balanceUpdates));

        verify(connection, times(1)).rollback();
    }

    @Test
    void shouldUpdateBalanceHandleCloseConnectionException() throws Exception {
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        doThrow(new SQLException("Message")).when(connection).close();

        final List<BalanceUpdate> balanceUpdates = Collections.singletonList(
                new BalanceUpdate("12345", BigDecimal.valueOf(12)));

        assertThrows(RepositoryException.class, () -> accountRepository.updateBalance(balanceUpdates));

        verify(connection, never()).rollback();
    }
}