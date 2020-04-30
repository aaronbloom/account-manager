package com.acmebank;

import com.acmebank.configuration.Configuration;
import com.acmebank.exception.ApplicationStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) throws ApplicationStartupException {
        logger.info("Starting account-manager.");

        final Configuration configuration = getConfiguration();

        final Startup startup = new Startup(configuration);

        startup.start();
    }

    public static Configuration getConfiguration() {
        return new Configuration(); // TODO source configuration from the environment
    }

}
