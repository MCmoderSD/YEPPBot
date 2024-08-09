package de.MCmoderSD.utilities.database;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public abstract class Driver {

    // Attributes
    protected final String host;
    protected final Integer port;
    protected final String database;
    protected final String username;
    protected final String password;

    // Connection
    protected Connection connection;

    // Constructor
    public Driver(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    // Constructor
    public Driver(JsonNode config) {
        this.host = config.get("host").asText();
        this.port = config.get("port").asInt();
        this.database = config.get("database").asText();
        this.username = config.get("username").asText();
        this.password = config.get("password").asText();
    }

    // Getter
    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        try {
            return connection != null && connection.isValid(0);
        } catch (SQLException e) {
            return false;
        }
    }

    // Setter
    public void connect() {
        try {
            if (isConnected()) return; // already connected
            connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password); // connect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (!isConnected()) return; // already disconnected
            connection.close(); // disconnect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
