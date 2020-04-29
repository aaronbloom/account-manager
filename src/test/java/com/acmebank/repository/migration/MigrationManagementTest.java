package com.acmebank.repository.migration;

import com.acmebank.exception.MigrationException;
import com.acmebank.exception.RepositoryException;
import com.acmebank.repository.instance.H2Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MigrationManagementTest {

    private H2Connection h2Connection;
    private Connection connection;
    private Statement statement;

    @BeforeEach
    void setUp() throws RepositoryException, SQLException {
        h2Connection = mock(H2Connection.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);

        when(h2Connection.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    void shouldCreateMigrationTableWithNoMigrations() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(statement.executeQuery(eq("SELECT * FROM migration"))).thenReturn(resultSet);

        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);

        migrationManagement.applyMigrations(Collections.emptyList());

        verify(statement, times(1))
                .execute(eq("CREATE TABLE IF NOT EXISTS migration(id INT PRIMARY KEY)"));
        verify(statement, times(1)).executeQuery(eq("SELECT * FROM migration"));
    }

    @Test
    void shouldApplyMigrations() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(statement.executeQuery(eq("SELECT * FROM migration"))).thenReturn(resultSet);

        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);

        migrationManagement.applyMigrations(Arrays.asList(
                s -> s.execute("SQL HERE 0"),
                s -> s.execute("SQL HERE 1")
        ));

        verify(statement, times(1))
                .execute(eq("CREATE TABLE IF NOT EXISTS migration(id INT PRIMARY KEY)"));
        verify(statement, times(1)).executeQuery(eq("SELECT * FROM migration"));
        verify(statement, times(1)).execute(eq("SQL HERE 0"));
        verify(statement, times(1)).execute("INSERT INTO migration VALUES (0)");
        verify(statement, times(1)).execute(eq("SQL HERE 1"));
        verify(statement, times(1)).execute("INSERT INTO migration VALUES (1)");
    }

    @Test
    void shouldApplyOneNewMigration() throws Exception {
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(resultSet.getInt(eq("id")))
                .thenReturn(0)
                .thenReturn(1);
        when(statement.executeQuery(eq("SELECT * FROM migration"))).thenReturn(resultSet);

        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);

        migrationManagement.applyMigrations(Arrays.asList(
                s -> s.execute("SQL HERE 0"),
                s -> s.execute("SQL HERE 1"),
                s -> s.execute("NEW SQL MIGRATION")
        ));

        verify(statement, times(1))
                .execute(eq("CREATE TABLE IF NOT EXISTS migration(id INT PRIMARY KEY)"));
        verify(statement, times(1)).executeQuery(eq("SELECT * FROM migration"));
        verify(statement, never()).execute(eq("SQL HERE 0"));
        verify(statement, never()).execute(eq("SQL HERE 1"));
        verify(statement, times(1)).execute(eq("NEW SQL MIGRATION"));
        verify(statement, times(1)).execute("INSERT INTO migration VALUES (2)");
    }

    @Test
    void shouldThrowMigrationExceptionUponCaughtRepositoryException() throws Exception {
        final H2Connection h2Connection = mock(H2Connection.class);
        when(h2Connection.getConnection()).thenThrow(new RepositoryException("boom"));
        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);

        assertThrows(MigrationException.class, () -> migrationManagement.applyMigrations(Collections.emptyList()));
    }

    // Would consider doing more scenarios, such as migration index integrity checks.
    // It would be best to rely on a more robust migration framework.
}