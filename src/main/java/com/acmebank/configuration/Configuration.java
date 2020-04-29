package com.acmebank.configuration;

public class Configuration {
    // Pre-populated with sensible default configuration
    private int httpPort = 8080;
    private String h2DatabaseConnectionUrl = "jdbc:h2:~/account-manager-db";
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
}
