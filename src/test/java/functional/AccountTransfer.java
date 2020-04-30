package functional;

import com.acmebank.Startup;
import com.acmebank.configuration.Configuration;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
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
public class AccountTransfer {

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
    void shouldTransferBetweenAccounts() throws Exception {
        try (final Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO account VALUES(default, 'TESTACCOUNT', 50)");
            statement.execute("INSERT INTO account VALUES(default, 'TARGETACCOUNT', 100)");
        }

        final String url = String.format("http://localhost:%d/api/account/transfer/from/%s/to/%s/amount/%d", HTTP_PORT, "TESTACCOUNT", "TARGETACCOUNT", 11);
        final HttpResponse<String> transferResponse = Unirest.post(url).asString();
        assertEquals(200, transferResponse.getStatus(), "Should be OK status response");
    }

    @Test
    void shouldTransferBetweenAccountsAndHaveCorrectBalances() throws Exception {
        try (final Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO account VALUES(default, 'TESTACCOUNT', 50)");
            statement.execute("INSERT INTO account VALUES(default, 'TARGETACCOUNT', 100)");
        }

        final String url = String.format("http://localhost:%d/api/account/transfer/from/%s/to/%s/amount/%d", HTTP_PORT, "TESTACCOUNT", "TARGETACCOUNT", 11);
        Unirest.post(url).asBinary();

        final String fromBalanceUrl = String.format("http://localhost:%d/api/account/balance/%s", HTTP_PORT, "TESTACCOUNT");
        final HttpResponse<JsonNode> fromAccountResponse = Unirest.get(fromBalanceUrl).asJson();
        assertEquals(new BigDecimal(39), fromAccountResponse.getBody().getObject().getBigDecimal("balance"), "From account should be correct balance");

        final String targetBalanceUrl = String.format("http://localhost:%d/api/account/balance/%s", HTTP_PORT, "TARGETACCOUNT");
        final HttpResponse<JsonNode> targetAccountResponse = Unirest.get(targetBalanceUrl).asJson();
        assertEquals(new BigDecimal(111), targetAccountResponse.getBody().getObject().getBigDecimal("balance"), "Target account should be correct balance");
    }

    @Test
    void shouldNotTransferBetweenAccountsWhenNegativeAmount() throws Exception {
        final String url = String.format("http://localhost:%d/api/account/transfer/from/%s/to/%s/amount/%d", HTTP_PORT, "TESTACCOUNT", "TARGETACCOUNT", -1);
        final HttpResponse<String> transferResponse = Unirest.post(url).asString();

        assertEquals(400, transferResponse.getStatus(), "Should be bad request status response");
        final String expected = "Path parameter 'amount' with value '-1' invalid - amount parameter must be greater than zero";
        assertEquals(expected, transferResponse.getBody(), "Should have message");
    }

    @Test
    void shouldNotTransferBetweenAccountsWhenAmountExceedsBalance() throws Exception {
        try (final Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO account VALUES(default, 'TESTACCOUNT', 50)");
            statement.execute("INSERT INTO account VALUES(default, 'TARGETACCOUNT', 100)");
        }

        final String url = String.format("http://localhost:%d/api/account/transfer/from/%s/to/%s/amount/%d", HTTP_PORT, "TESTACCOUNT", "TARGETACCOUNT", 51);
        final HttpResponse<String> transferResponse = Unirest.post(url).asString();

        assertEquals(400, transferResponse.getStatus(), "Should be bad request status response");
        assertEquals("Invalid request state", transferResponse.getBody(), "Should have message");
    }


}
