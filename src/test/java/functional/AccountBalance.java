package functional;

import com.acmebank.Startup;
import com.acmebank.api.response.AccountBalanceResponse;
import com.acmebank.configuration.Configuration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.javalin.plugin.json.JavalinJson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("Functional")
public class AccountBalance {

    private final static int HTTP_PORT = 50505;
    private final static String DB_URL = "jdbc:h2:mem:test-db";
    private Connection connection;
    private Startup startup;

    @BeforeEach
    void setUp() throws Exception {
        Unirest.setConcurrency(1, 1); // issue with TCP stack if not set
        connection = DriverManager.getConnection(DB_URL);
        final Configuration configuration = new Configuration();
        configuration.setHttpPort(HTTP_PORT);
        configuration.setH2DatabaseConnectionUrl(DB_URL);
        startup = new Startup(configuration);
        startup.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
        startup.stop();
    }

    @Test
    void shouldGetBalance() throws Exception {
        try (final Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO account VALUES(default, 'TEST-ACCOUNT', 42)");
        }

        final String url = String.format("http://localhost:%d/api/account/balance/%s", HTTP_PORT, "TEST-ACCOUNT");
        final HttpResponse<String> response = Unirest.get(url).asString();

        final String expected = JavalinJson.toJson(new AccountBalanceResponse("TEST-ACCOUNT", new BigDecimal(42)));

        assertEquals(200, response.getStatus(), "Should be OK status response");
        assertEquals(expected, response.getBody(), "Should be correct response body");
    }

    @Test
    void shouldGetBalanceButNotFindAccount() throws Exception {
        final String url = String.format("http://localhost:%d/api/account/balance/%s", HTTP_PORT, "TEST-ACCOUNT");
        final HttpResponse<String> response = Unirest.get(url).asString();

        assertEquals(404, response.getStatus(), "Should be OK status response");
        assertEquals("Account not found 'TEST-ACCOUNT'", response.getBody(), "Should be correct response body");
    }
}
