package de.MCmoderSD.utilities.database;

import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.utilities.json.JsonNode;

import java.sql.*;

public class MySQL {

    // Associations
    private final Frame frame;

    // Attributes
    private final String host;
    private final int port;
    private final String database;
    private final String table;
    private final String username;
    private final String password;

    // Variables
    private Connection connection;

    // Constructor
    public MySQL(JsonNode databaseConfig, Frame frame) {
        host = databaseConfig.get("host").asText();
        port = databaseConfig.get("port").asInt();
        database = databaseConfig.get("database").asText();
        table = databaseConfig.get("table").asText();
        username = databaseConfig.get("username").asText();
        password = databaseConfig.get("password").asText();
        connect();
        this.frame = frame;
    }


    // Connect to MySQL
    public void connect() {
        try {
            if (isConnected()) return; // already connected
            connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password); // connect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Disconnect from MySQL
    @SuppressWarnings("unused")
    public void disconnect() {
        try {
            if (!isConnected()) return; // already disconnected
            connection.close(); // disconnect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Write data to MySQL
    public void log(Date date, Time time, String type, String channel, String author, String message) {
        try {
            if (!isConnected()) connect();

            String query = "INSERT INTO " + table + " (date, time, type, channel, author, message) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDate(1, date);
            preparedStatement.setTime(2, time);
            preparedStatement.setString(3, type);
            preparedStatement.setString(4, channel);
            preparedStatement.setString(5, author);
            preparedStatement.setString(6, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (frame != null) frame.log(type, channel, author, message);
    }

    // Getter
    public boolean isConnected() {
        return connection != null;
    }
}