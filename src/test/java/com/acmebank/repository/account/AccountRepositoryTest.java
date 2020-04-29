package com.acmebank.repository.account;

import com.acmebank.exception.AccountNotFoundException;
import com.acmebank.exception.RepositoryException;
import com.acmebank.repository.instance.H2Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}