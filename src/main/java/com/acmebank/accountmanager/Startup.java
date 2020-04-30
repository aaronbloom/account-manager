package com.acmebank.accountmanager;

import com.acmebank.accountmanager.api.controller.AccountController;
import com.acmebank.accountmanager.bank.BankManager;
import com.acmebank.accountmanager.configuration.Configuration;
import com.acmebank.accountmanager.exception.ApplicationStartupException;
import com.acmebank.accountmanager.exception.MigrationException;
import com.acmebank.accountmanager.repository.instance.H2Connection;
import com.acmebank.accountmanager.repository.migration.Migrations;
import com.acmebank.accountmanager.api.HttpApi;
import com.acmebank.accountmanager.repository.account.AccountRepository;
import com.acmebank.accountmanager.repository.migration.MigrationManagement;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Startup {
    private final static Logger logger = LoggerFactory.getLogger(Startup.class);

    private final Configuration configuration;
    private Javalin javalinServer;

    public Startup(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void start() throws ApplicationStartupException {
        // Simple inversion of control wiring
        logger.info("Setting up dependencies.");
        final H2Connection h2Connection = new H2Connection(configuration.getH2DatabaseConnectionUrl(),
                configuration.getH2DatabaseConnectionUserName(), configuration.getH2DatabaseConnectionPassword());
        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);
        final AccountRepository accountRepository = new AccountRepository(h2Connection);
        final BankManager bankManager = new BankManager(accountRepository);
        final AccountController accountController = new AccountController(bankManager);
        final HttpApi httpApi = new HttpApi(accountController);

        logger.info("Setting up pre-startup.");
        try {
            migrationManagement.applyMigrations(Migrations.getMigrations());
        } catch (final MigrationException e) {
            final String message = "Unable to setup application, migrations failed.";
            logger.info(message);
            throw new ApplicationStartupException(message, e);
        }

        logger.info("Setting up HTTP server.");
        httpApi.registerDataTypes();
        javalinServer = httpApi.create();
        httpApi.registerRoutes(javalinServer);
        javalinServer.start(configuration.getHttpPort());

        logger.info("Application started.");
    }

    public void stop() {
        if (javalinServer != null) {
            javalinServer.stop();
        }
    }

}
