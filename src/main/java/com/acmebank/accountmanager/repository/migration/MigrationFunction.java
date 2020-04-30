package com.acmebank.accountmanager.repository.migration;

import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface MigrationFunction {
    void apply(final Statement statement) throws SQLException;
}
