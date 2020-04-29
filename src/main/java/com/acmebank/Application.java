package com.acmebank;

import com.acmebank.api.AccountController;
import com.acmebank.api.AccountManagerApiHandler;
import com.acmebank.bank.BankManager;
import com.acmebank.exception.MigrationException;
import com.acmebank.repository.account.AccountRepository;
import com.acmebank.repository.instance.H2Connection;
import com.acmebank.repository.migration.MigrationManagement;
import com.acmebank.repository.migration.Migrations;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) throws Exception {
        logger.info("Starting account-manager.");

        final Server server = new Server(8080); // TODO config

        server.setHandler(ioc());

        try {
            server.start();
        } catch (final Exception e) {
            logger.error("Unable to start Jetty server", e);
            throw e;
        }

        try {
            server.join();
        } catch (final InterruptedException e) {
            logger.error("Unable to join Jetty to the thread pool", e);
            throw e;
        }
    }

    // maybe not static and pass in configuration object
    public static AccountManagerApiHandler ioc() throws MigrationException {

        // Wiring
        final H2Connection h2Connection = new H2Connection("jdbc:h2:~/account-manager-db", "", "");
        final MigrationManagement migrationManagement = new MigrationManagement(h2Connection);
        final AccountRepository accountRepository = new AccountRepository(h2Connection);
        final BankManager bankManager = new BankManager(accountRepository);
        final AccountController accountController = new AccountController(bankManager);
        final AccountManagerApiHandler accountManagerApiHandler = new AccountManagerApiHandler(accountController);

        // App start-up
        migrationManagement.applyMigrations(Migrations.getMigrations());

        return accountManagerApiHandler;
    }

}
