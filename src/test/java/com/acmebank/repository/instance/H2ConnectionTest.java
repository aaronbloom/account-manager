package com.acmebank.repository.instance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class H2ConnectionTest {

    @Test
    void shouldGetConnection() {
        final H2Connection h2Connection = new H2Connection("jdbc:h2:~/test", "admin", "admin");

//        h2Connection.getConnection();

        // TODO - perhaps have driver manager as a dependency
    }
}