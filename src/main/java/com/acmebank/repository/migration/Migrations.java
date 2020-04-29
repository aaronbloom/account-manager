package com.acmebank.repository.migration;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public final class Migrations {

    public static List<MigrationFunction> getMigrations() {
        // Ideally these migrations would be handled externally, as part of the deployment.
        // These migrations also do not currently support rollbacks or checksums.
        // Consider using a framework such as Flyway to manage DB migrations.
        return Arrays.asList(
                (final Statement statement) -> {
                    statement.execute("CREATE TABLE account(id IDENTITY NOT NULL, accountId VARCHAR(255) NOT NULL UNIQUE, balance DECIMAL)");
                },
                (final Statement statement) -> {
                    // The following is a data migration, for this exercise this will be fine
                    statement.execute("INSERT INTO account VALUES(default, '12345678', 1000000)");
                    statement.execute("INSERT INTO account VALUES(default, '88888888', 1000000)");
                }
                // Future migrations to be appended here as appropriate
        );
    }

}
