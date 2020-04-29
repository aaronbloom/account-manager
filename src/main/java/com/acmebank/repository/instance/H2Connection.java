package com.acmebank.repository.instance;

import com.acmebank.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Connection {
    private final static Logger logger = LoggerFactory.getLogger(H2Connection.class);

    private final String connectionUrl;
    private final String userName;
    private final String password;

    public H2Connection(final String connectionUrl, final String userName, final String password) {
        this.connectionUrl = connectionUrl;
        this.userName = userName;
        this.password = password;
    }

    public Connection getConnection() throws RepositoryException {
        logger.debug("Getting H2 database connection.");
        // Should consider using a pooled DB connection, currently new connection pre-request
        try {
            return DriverManager.getConnection(connectionUrl, userName, password);
        } catch (final SQLException e) {
            final String message = "Could not get connection";
            logger.error(message, e);
            throw new RepositoryException(message, e);
        }
    }

}
