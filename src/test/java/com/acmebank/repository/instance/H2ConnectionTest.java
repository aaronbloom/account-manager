package com.acmebank.repository.instance;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class H2ConnectionTest {

    @Test
    void canGetH2Connection() {
        final H2Connection h2Connection = new H2Connection("jdbc:h2:mem:test-db-memory", "admin", "admin");

        assertDoesNotThrow(() -> {
            try (final Connection connection = h2Connection.getConnection()) {
                assertTrue(connection.isValid(30), "Connection should be valid");
            }
        });
    }
}