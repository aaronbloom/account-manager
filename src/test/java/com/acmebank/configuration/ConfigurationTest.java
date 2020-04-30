package com.acmebank.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void shouldHaveDefaultConfiguration() {
        final Configuration configuration = new Configuration();

        assertEquals(8080, configuration.getHttpPort(), "Use default port");
        assertEquals("jdbc:h2:./database/account-manager-db", configuration.getH2DatabaseConnectionUrl(), "Use default h2 url");
        assertEquals("", configuration.getH2DatabaseConnectionUserName(), "Use default h2 username");
        assertEquals("", configuration.getH2DatabaseConnectionUserName(), "Use default h2 password");
    }

    @Test
    void shouldAttemptSourceFromEnvironmentVariables() {
        assertDoesNotThrow(Configuration::fromEnvironment);
    }
}