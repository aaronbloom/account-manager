package com.acmebank.accountmanager;

import com.acmebank.accountmanager.configuration.Configuration;
import com.acmebank.accountmanager.exception.ApplicationStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) throws ApplicationStartupException {
        logger.info("Starting account-manager.");

        final Configuration configuration = Configuration.fromEnvironment();

        final Startup startup = new Startup(configuration);

        startup.start();
    }

}
