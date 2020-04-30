package com.acmebank.accountmanager.repository.migration;

import com.acmebank.accountmanager.exception.MigrationException;
import com.acmebank.accountmanager.exception.RepositoryException;
import com.acmebank.accountmanager.repository.instance.H2Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MigrationManagement {
    private final static Logger logger = LoggerFactory.getLogger(MigrationManagement.class);

    private final H2Connection h2Connection;

    public MigrationManagement(final H2Connection h2Connection) {
        this.h2Connection = h2Connection;
    }

    public void applyMigrations(final List<MigrationFunction> migrations) throws MigrationException {
        logger.info("Evaluating {} migrations.", migrations.size());
        try (final Connection connection = h2Connection.getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                initialiseMigrationsIndex(statement);
                executeMigrations(migrations, statement, getMigrationIndexes(statement));
            }
        } catch (final SQLException e) {
            final String message = "Could not apply migrations";
            logger.error(message, e);
            throw new MigrationException(message, e);
        } catch (final RepositoryException e) {
            final String message = "Could not apply migrations, unable to get connection";
            logger.error(message, e);
            throw new MigrationException(message, e);
        }
    }

    private void executeMigrations(final List<MigrationFunction> migrations,
                                   final Statement statement,
                                   final List<Integer> migrationIndexes) throws SQLException {
        for (int i = 0; i < migrations.size(); i++) {
            if (migrationIndexes.contains(i)) {
                logger.info("Migration {} already applied, moving to next migration.", i);
            } else {
                logger.info("Applying migration {}.", i);
                final MigrationFunction migration = migrations.get(i);
                migration.apply(statement);
                final String insertMigrationIndex = String.format("INSERT INTO migration VALUES (%d)", i);
                statement.execute(insertMigrationIndex);
                logger.info("Applied migration {}, updated DB migration index.", i);
            }
        }
    }

    private List<Integer> getMigrationIndexes(final Statement statement) throws SQLException {
        final List<Integer> migrationIndexes = new ArrayList<>();
        try (final ResultSet results = statement.executeQuery("SELECT * FROM migration")) {
            while (results.next()) {
                migrationIndexes.add(results.getInt("id"));
            }
        }
        logger.info("{} migrations currently present in DB.", migrationIndexes.size());
        return migrationIndexes;
    }

    private void initialiseMigrationsIndex(final Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS migration(id INT PRIMARY KEY)");
    }

}
