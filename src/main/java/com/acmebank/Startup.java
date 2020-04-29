package com.acmebank;

import com.acmebank.api.controller.AccountController;
import com.acmebank.bank.BankManager;
import com.acmebank.configuration.Configuration;
import com.acmebank.exception.ApplicationStartupException;
import com.acmebank.exception.MigrationException;
import com.acmebank.repository.account.AccountRepository;
import com.acmebank.repository.instance.H2Connection;
import com.acmebank.repository.migration.MigrationManagement;
import com.acmebank.repository.migration.Migrations;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Startup {
    private final static Logger logger = LoggerFactory.getLogger(Startup.class);

    private final Configuration configuration;

    public Startup(final Configuration configuration) {
        this.configuration = configuration;
    }

    public void start() throws ApplicationStartupException {
        // Simple inversion of control wiring
        logger.info("Dependencies setup.");
        final H2Connection h2Connection = new H2Connection(configuration.getH2DatabaseConnectionUrl(),
                configuration.getH2DatabaseConnectionUserName(), configuration.getH2DatabaseConnectionPassword());
        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);
        final AccountRepository accountRepository = new AccountRepository(h2Connection);
        final BankManager bankManager = new BankManager(accountRepository);
        final AccountController accountController = new AccountController(bankManager);

        logger.info("Pre-startup setup.");
        // Pre-startup setup
        try {
            migrationManagement.applyMigrations(Migrations.getMigrations());
        } catch (final MigrationException e) {
            final String message = "Unable to setup application, migrations failed.";
            logger.info(message);
            throw new ApplicationStartupException(message, e);
        }

        logger.info("HTTP server setup.");
        // Start HTTP server
        final Javalin javalinServer = Javalin.create();

        javalinServer.routes(() -> {
            path("api/account", () -> {
                path("balance/:accountId", () -> {
                    get(accountController::getBalance);
                });
                path("/transfer/from/:accountId/to/:targetAccountId/amount/:amount", () -> {
                    post(accountController::transfer);
                });
            });
        });

        javalinServer.start(configuration.getHttpPort());

        logger.info("Application started.");
    }

}
