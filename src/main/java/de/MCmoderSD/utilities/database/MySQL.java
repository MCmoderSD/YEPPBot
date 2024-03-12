package de.MCmoderSD.utilities.database;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import de.MCmoderSD.UI.Frame;
import de.MCmoderSD.utilities.json.JsonNode;

import java.sql.*;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings("unused")
public class MySQL {

    // Associations
    private final Frame frame;

    // Attributes
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    // Variables
    private Connection connection;

    // Constructor
    public MySQL(JsonNode databaseConfig, Frame frame) {
        host = databaseConfig.get("host").asText();
        port = databaseConfig.get("port").asInt();
        database = databaseConfig.get("database").asText();
        username = databaseConfig.get("username").asText();
        password = databaseConfig.get("password").asText();
        connect();
        this.frame = frame;
    }

    // Checks Channels
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkChannel(ChannelMessageEvent event) {
        int id = getChannelID(event);
        String name = getChannel(event);

        try {
            if (!isConnected()) connect();

            String query = "SELECT * FROM " + "channels" + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                query = "INSERT INTO " + "channels" + " (id, name) VALUES (?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, name);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Checks Users
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkUser(ChannelMessageEvent event) {
        int id = getUserID(event);
        String name = getAuthor(event);

        try {
            if (!isConnected()) connect();

            String query = "SELECT * FROM " + "users" + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                query = "INSERT INTO " + "users" + " (id, name) VALUES (?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, name);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Log Message
    public void logMessage(ChannelMessageEvent event) {
        checkChannel(event);
        checkUser(event);

        int channelID = getChannelID(event);
        int userID = getUserID(event);
        String message = getMessage(event);

        try {
            if (!isConnected()) connect();

            String query = "INSERT INTO " + "MessageLog" + " (timestamp, channel_id, user_id, message) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, getTimestamp());
            preparedStatement.setInt(2, channelID);
            preparedStatement.setInt(3, userID);
            preparedStatement.setString(4, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (frame != null) frame.log(MESSAGE, getChannel(event), getAuthor(event), message);
    }

    // Log Command
    public void logCommand(ChannelMessageEvent event, String command, String args) {
        checkChannel(event);
        checkUser(event);

        int channelID = getChannelID(event);
        int userID = getUserID(event);

        try {
            if (!isConnected()) connect();

            String query = "INSERT INTO " + "CommandLog" + " (timestamp, channel_id, user_id, command, args) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, getTimestamp());
            preparedStatement.setInt(2, channelID);
            preparedStatement.setInt(3, userID);
            preparedStatement.setString(4, command);
            preparedStatement.setString(5, args);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (frame != null) frame.log(COMMAND, getChannel(event), getAuthor(event), command);
    }

    // Log Response
    public void logResponse(ChannelMessageEvent event, String command, String args, String response) {
        checkChannel(event);
        checkUser(event);

        int channelID = getChannelID(event);
        int userID = getUserID(event);

        try {
            if (!isConnected()) connect();

            String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, getTimestamp());
            preparedStatement.setInt(2, channelID);
            preparedStatement.setInt(3, userID);
            preparedStatement.setString(4, command);
            preparedStatement.setString(5, args);
            preparedStatement.setString(6, response);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (frame != null) frame.log(SYSTEM, getChannel(event), getAuthor(event), response);
    }

    // Log Message Sent
    public void messageSent(String channel, String botName, String message) {
        try {
            if (!isConnected()) connect();

            String query = "INSERT INTO " + "MessageLog" + " (timestamp, channel_id, user_id, message) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, getTimestamp());
            preparedStatement.setInt(2, queryChannelID(channel));
            preparedStatement.setInt(3, queryUserID(botName));
            preparedStatement.setString(4, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        if (frame != null) frame.log(MESSAGE, channel, botName, message);
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
        try {
            if (isConnected()) return; // already connected
            connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password); // connect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void disconnect() {
        try {
            if (!isConnected()) return; // already disconnected
            connection.close(); // disconnect
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}