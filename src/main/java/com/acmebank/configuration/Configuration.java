package com.acmebank.configuration;

public class Configuration {
    // Pre-populated with sensible default configuration
    private int httpPort = 8080;
    private String h2DatabaseConnectionUrl = "jdbc:h2:./database/account-manager-db";
    private String h2DatabaseConnectionUserName = "";
    private String h2DatabaseConnectionPassword = "";

    public void setHttpPort(final int httpPort) {
        this.httpPort = httpPort;
    }

    public void setH2DatabaseConnectionUrl(final String h2DatabaseConnectionUrl) {
        this.h2DatabaseConnectionUrl = h2DatabaseConnectionUrl;
    }

    public void setH2DatabaseConnectionUserName(final String h2DatabaseConnectionUserName) {
        this.h2DatabaseConnectionUserName = h2DatabaseConnectionUserName;
    }

    public void setH2DatabaseConnectionPassword(final String h2DatabaseConnectionPassword) {
        this.h2DatabaseConnectionPassword = h2DatabaseConnectionPassword;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getH2DatabaseConnectionUrl() {
        return h2DatabaseConnectionUrl;
    }

    public String getH2DatabaseConnectionUserName() {
        return h2DatabaseConnectionUserName;
    }

    public String getH2DatabaseConnectionPassword() {
        return h2DatabaseConnectionPassword;
    }

    public static Configuration fromEnvironment() {
        final Configuration configuration = new Configuration();

        final String accountManagerPort = System.getenv("ACCOUNT_MANAGER_PORT");
        if (isEnvironmentVariableSet(accountManagerPort)) {
            final int port = Integer.parseInt(accountManagerPort);
            configuration.setHttpPort(port);
        }

        final String accountManagerH2Url = System.getenv("ACCOUNT_MANAGER_H2_URL");
        if (isEnvironmentVariableSet(accountManagerH2Url)) {
            configuration.setH2DatabaseConnectionUrl(accountManagerH2Url);
        }

        final String accountManagerH2UserName = System.getenv("ACCOUNT_MANAGER_H2_USERNAME");
        if (isEnvironmentVariableSet(accountManagerH2UserName)) {
            configuration.setH2DatabaseConnectionUserName(accountManagerH2UserName);
        }

        final String accountManagerH2Password = System.getenv("ACCOUNT_MANAGER_H2_PASSWORD");
        if (isEnvironmentVariableSet(accountManagerH2Password)) {
            configuration.setH2DatabaseConnectionPassword(accountManagerH2Password);
        }

        return configuration;
    }

    private static boolean isEnvironmentVariableSet(final String value) {
        return value != null && !value.equals("");
    }
}
