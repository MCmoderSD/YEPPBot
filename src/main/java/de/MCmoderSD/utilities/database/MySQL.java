package de.MCmoderSD.utilities.database;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.UI.Frame;

import de.MCmoderSD.utilities.json.JsonNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings("unused")
public class MySQL {

    // Associations
    private final Frame frame;

    // Attributes
    private final String host;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean isActive;

    // Variables
    private Connection connection;

    // Constructor
    public MySQL(JsonNode databaseConfig, Frame frame) {

        // Set Attributes
        host = databaseConfig.get("host").asText();
        port = databaseConfig.get("port").asInt();
        database = databaseConfig.get("database").asText();
        username = databaseConfig.get("username").asText();
        password = databaseConfig.get("password").asText();

        // Check if active
        isActive = host != null && database != null && username != null && password != null;

        // Connect to database
        new Thread(this::connect).start();

        // Set Frame
        this.frame = frame;
    }

    public MySQL(Frame frame) {

        // Set Attributes
        host = null;
        port = null;
        database = null;
        username = null;
        password = null;

        // Check if active
        isActive = false;

        // Set Frame
        this.frame = frame;
    }

    // Checks Channels
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkChannel(ChannelMessageEvent event) {

        // Return if not active
        if (!isActive) return;

        // New Thread
        new Thread(() -> {

            // Set Variables
            int id = getChannelID(event);
            String name = getChannel(event);

            // Check Channel
            try {
                if (!isConnected()) connect(); // connect

                // Check Channel
                String query = "SELECT * FROM " + "channels" + " WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();

                // Add Channel
                if (!resultSet.next()) {
                    query = "INSERT INTO " + "channels" + " (id, name) VALUES (?, ?)";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, id); // set id
                    preparedStatement.setString(2, name); // set name
                    preparedStatement.executeUpdate(); // execute
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Checks Users
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkUser(ChannelMessageEvent event) {

        // Return if not active
        if (!isActive) return;

        // New Thread
        new Thread(() -> {

            // Set Variables
            int id = getUserID(event);
            String name = getAuthor(event);

            // Check User
            try {
                if (!isConnected()) connect(); // connect

                // Check User
                String query = "SELECT * FROM " + "users" + " WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();

                // Add User
                if (!resultSet.next()) {
                    query = "INSERT INTO " + "users" + " (id, name) VALUES (?, ?)";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, id); // set id
                    preparedStatement.setString(2, name); // set name
                    preparedStatement.executeUpdate(); // execute
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Message
    public void logMessage(ChannelMessageEvent event) {
        new Thread(() -> {

            // Set Variables
            int channelID = getChannelID(event);
            int userID = getUserID(event);
            String message = getMessage(event);

            // Update Frame
            if (frame != null) frame.log(MESSAGE, getChannel(event), getAuthor(event), message);

            // Return if not active
            if (!isActive) return;

            // Check Channel and User
            checkChannel(event);
            checkUser(event);

            // Log message
            try {
                if (!isConnected()) connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "MessageLog" + " (timestamp, channel_id, user_id, message) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, message); // set message
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Command
    public void logCommand(ChannelMessageEvent event, String command, String args) {
        new Thread(() -> {

            // Set Variables
            int channelID = getChannelID(event);
            int userID = getUserID(event);

            // Update Frame
            if (frame != null) frame.log(COMMAND, getChannel(event), getAuthor(event), command);

            // Return if not active
            if (!isActive) return;

            // Check Channel and User
            checkChannel(event);
            checkUser(event);

            // Log Command
            try {
                if (!isConnected()) connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "CommandLog" + " (timestamp, channel_id, user_id, command, args) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, command); // set command
                preparedStatement.setString(5, args); // set args
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Response
    public void logResponse(ChannelMessageEvent event, String command, String args, String response) {
        new Thread(() -> {

            // Set Variables
            int channelID = getChannelID(event);
            int userID = getUserID(event);

            // Update Frame
            if (frame != null) frame.log(SYSTEM, getChannel(event), getAuthor(event), response);

            // Return if not active
            if (!isActive) return;

            // Check Channel and User
            checkChannel(event);
            checkUser(event);

            // Log Response
            try {
                if (!isConnected()) connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, command); // set command
                preparedStatement.setString(5, args); // set args
                preparedStatement.setString(6, response); // set response
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Message Sent
    public void messageSent(String channel, String botName, String message) {
        new Thread(() -> {

            // Update Frame
            if (frame != null) frame.log(MESSAGE, channel, botName, message);

            // Return if not active
            if (!isActive) return;

            // Log Message Sent
            try {
                if (!isConnected()) connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "MessageLog" + " (timestamp, channel_id, user_id, message) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, queryChannelID(channel)); // set channel
                preparedStatement.setInt(3, queryUserID(botName)); // set user
                preparedStatement.setString(4, message); // set message
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Query Channel ID
    public int queryChannelID(String channel) {
        try {
            if (!isConnected()) connect();

            String query = "SELECT id FROM " + "channels" + " WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, channel);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("id");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    // Query User ID
    public int queryUserID(String user) {
        try {
            if (!isConnected()) connect();

            String query = "SELECT id FROM " + "users" + " WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("id");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    // Query Channel
    public String queryChannel(int id) {
        try {
            if (!isConnected()) connect();

            String query = "SELECT name FROM " + "channels" + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("name");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Query User
    public String queryUser(int id) {
        try {
            if (!isConnected()) connect();

            String query = "SELECT name FROM " + "users" + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("name");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // Getter
    public boolean isConnected() {
        return connection != null;
    }

    // Setter
    public void connect() {
        if (!isActive) return;
        try {
            if (isConnected()) return; // already connected
            connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password); // connect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void disconnect() {
        if (!isActive) return;
        try {
            if (!isConnected()) return; // already disconnected
            connection.close(); // disconnect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}