package de.MCmoderSD.utilities.database;

import com.fasterxml.jackson.databind.JsonNode;
import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MySQL {

    // Attributes
    private final String host;
    private final Integer port;
    private final String database;
    private final String username;
    private final String password;

    // Cache Lists
    private final HashMap<Integer, String> channelCache;
    private final HashMap<Integer, String> userCache;

    // Variables
    private Connection connection;
    private boolean log;

    // Constructor
    public MySQL(Main main) {

        // Set Associations
        JsonNode databaseConfig = main.getCredentials().getMySQLConfig();

        // Set Attributes
        host = databaseConfig.get("host").asText();
        port = databaseConfig.get("port").asInt();
        database = databaseConfig.get("database").asText();
        username = databaseConfig.get("username").asText();
        password = databaseConfig.get("password").asText();

        // Set Logging
        log = main.getArg("log");

        // Initialize Cache Lists
        channelCache = new HashMap<>();
        userCache = new HashMap<>();

        // Connect to database
        new Thread(this::connect).start();
        init();

        // Load Cache
        loadChannelCache();
        loadUserCache();
    }

    private void init() {
        try {
            if (!isConnected()) connect();

            // List of tables to be created
            ArrayList<PreparedStatement> tables = new ArrayList<>();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the users table
            tables.add(
                    connection.prepareStatement(condition + "users (" +
                            "id INT PRIMARY KEY, " +
                            "name VARCHAR(25) NOT NULL" +
                            ")"));

            // SQL statement for creating the channels table
            tables.add(
                    connection.prepareStatement(condition + "channels (" +
                            "id INT PRIMARY KEY, " +
                            "name VARCHAR(25) NOT NULL, " +
                            "blacklist TEXT, " +
                            "active BIT NOT NULL DEFAULT 1" +
                            ")"));

            // SQL statement for creating the message log table
            tables.add(
                    connection.prepareStatement(condition + "MessageLog (" +
                            "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "type VARCHAR(5) NOT NULL, " +
                            "channel_id INT NOT NULL, " +
                            "user_id INT NOT NULL, " +
                            "message VARCHAR(500), " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id)" +
                            ")"));

            // SQL statement for creating the command log table
            tables.add(
                    connection.prepareStatement(condition + "CommandLog (" +
                            "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "channel_id INT NOT NULL, " +
                            "user_id INT NOT NULL, " +
                            "command TEXT NOT NULL, " +
                            "args VARCHAR(500), " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id)" +
                            ")"));

            // SQL statement for creating the response log table
            tables.add(
                    connection.prepareStatement(condition + "ResponseLog (" +
                            "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "channel_id INT NOT NULL, " +
                            "user_id INT NOT NULL, " +
                            "command TEXT NOT NULL, " +
                            "args VARCHAR(500), " +
                            "response VARCHAR(500), " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id)" +
                            ")"));

            // SQL statement for creating the lurk list table
            tables.add(
                    connection.prepareStatement(condition + "lurkList (" +
                            "user_id INT PRIMARY KEY, " +
                            "lurkChannel_ID INT NOT NULL, " +
                            "startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                            "traitorChannel TEXT, " +
                            "FOREIGN KEY (lurkChannel_ID) REFERENCES channels(id)" +
                            ")"));

            // SQL statement for creating the custom timers table
            tables.add(
                    connection.prepareStatement(condition + "CustomTimers (" +
                            "channel_id INT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "time TEXT NOT NULL, " +
                            "response VARCHAR(500) NOT NULL, " +
                            "isEnabled BIT NOT NULL DEFAULT 1, " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id)" +
                            ")"));

            // SQL statement for creating the custom commands table
            tables.add(
                    connection.prepareStatement(condition + "CustomCommands (" +
                            "channel_id INT NOT NULL, " +
                            "command_name TEXT NOT NULL, " +
                            "command_alias TEXT, " +
                            "command_response VARCHAR(500) NOT NULL, " +
                            "isEnabled BIT NOT NULL DEFAULT 1, " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id)" +
                            ")"));

            // SQL statement for creating the counters table
            tables.add(
                    connection.prepareStatement(condition + "Counters (" +
                            "channel_id INT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "value INT NOT NULL, " +
                            "FOREIGN KEY (channel_id) REFERENCES channels(id)" +
                            ")"));

            // SQL statement for creating the fact list table
            tables.add(
                    connection.prepareStatement(condition + "factList (" +
                            "fact_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "en_percent varchar(500), " +
                            "en_people varchar(500), " +
                            "en_verb varchar(500), " +
                            "en_frequency varchar(500), " +
                            "en_adjective varchar(500), " +
                            "en_object varchar(500), " +
                            "de_percent varchar(500), " +
                            "de_people varchar(500), " +
                            "de_verb varchar(500), " +
                            "de_frequency varchar(500), " +
                            "de_adjective varchar(500), " +
                            "de_object varchar(500)" +
                            ")"));

            // SQL statement for creating the joke list table
            tables.add(
                    connection.prepareStatement(condition + "jokeList (" +
                            "joke_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "en varchar(500), " +
                            "de varchar(500)" +
                            ")"));

            // SQL statement for creating the insult list table
            tables.add(
                    connection.prepareStatement(condition + "insultList (" +
                            "insult_id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "en varchar(500) NOT NULL, " +
                            "de varchar(500) NOT NULL" +
                            ")"));

            // Execute each SQL statement in the list tables
            for (PreparedStatement table : tables) table.execute();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Load Channel Cache
    private void loadChannelCache() {

        // New Thread
        new Thread(() -> {

            // Load Channel Cache
            try {
                if (!isConnected()) connect(); // connect

                String query = "SELECT * FROM " + "channels";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) channelCache.put(resultSet.getInt("id"), resultSet.getString("name")); // add to cache
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Load User Cache
    private void loadUserCache() {

        // New Thread
        new Thread(() -> {

            // Load User Cache
            try {
                if (!isConnected()) connect(); // connect

                String query = "SELECT * FROM " + "users";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) userCache.put(resultSet.getInt("id"), resultSet.getString("name")); // add to cache

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Checks Channels
    public void checkChannel(int id, String name) {
        if (channelCache.containsKey(id)) return;

        // New Thread
        new Thread(() -> {

            // Check Channel
            try {
                if (!isConnected()) connect(); // connect

                // Check Channel
                String selectQuery = "SELECT * FROM channels WHERE id = ?";
                PreparedStatement selectPreparedStatement = connection.prepareStatement(selectQuery);
                selectPreparedStatement.setInt(1, id);
                ResultSet resultSet = selectPreparedStatement.executeQuery();

                // Add Channel
                if (!resultSet.next()) {
                    String insertQuery = "INSERT INTO channels (id, name) VALUES (?, ?)";
                    PreparedStatement insertPreparedStatement = connection.prepareStatement(insertQuery);
                    insertPreparedStatement.setInt(1, id); // set id
                    insertPreparedStatement.setString(2, name); // set name
                    insertPreparedStatement.executeUpdate(); // execute
                    insertPreparedStatement.close(); // close the insertPreparedStatement
                }

                // Add to Cache
                channelCache.put(id, name);

                // Close resources
                resultSet.close();
                selectPreparedStatement.close(); // close the selectPreparedStatement

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Checks Users
    public void checkUser(int id, String name) {

        // Return if in Cache
        if (userCache.containsKey(id)) return;

        // New Thread
        new Thread(() -> {

            // Check User
            try {
                if (!isConnected()) connect(); // connect

                // Check User
                String selectQuery = "SELECT * FROM users WHERE id = ?";
                PreparedStatement selectPreparedStatement = connection.prepareStatement(selectQuery);
                selectPreparedStatement.setInt(1, id);
                ResultSet resultSet = selectPreparedStatement.executeQuery();

                // Add User
                if (!resultSet.next()) {
                    String insertQuery = "INSERT INTO users (id, name) VALUES (?, ?)";
                    PreparedStatement insertPreparedStatement = connection.prepareStatement(insertQuery);
                    insertPreparedStatement.setInt(1, id); // set id
                    insertPreparedStatement.setString(2, name); // set name
                    insertPreparedStatement.executeUpdate(); // execute
                    insertPreparedStatement.close(); // close the insertPreparedStatement
                }

                // Add to Cache
                userCache.put(id, name);

                // Close resources
                resultSet.close();
                selectPreparedStatement.close(); // close the selectPreparedStatement

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Message
    public void logMessage(TwitchMessageEvent event) {
        if (log) event.logToMySQL(this); // log to MySQL
    }

    // Log Response
    public void logResponse(TwitchMessageEvent event, String command, String response) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            checkChannel(channelID, channel); // check channel
            checkUser(userID, user); // check user

            try {
                if (!isConnected()) connect(); // connect

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, event.getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, command); // set command
                preparedStatement.setString(5, event.getMessage()); // set args
                preparedStatement.setString(6, response); // set response
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Get Active Channels
    public ArrayList<String> getActiveChannels() {

        // Variables
        ArrayList<String> channels = new ArrayList<>();

        try {
            if (!isConnected()) connect();

            String query = "SELECT name FROM " + "channels" + " WHERE active = 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) channels.add(resultSet.getString("name"));
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return channels;
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
        try {
            return connection != null && connection.isValid(0);
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isLoggingEnabled() {
        return log;
    }

    public Connection getConnection() {
        return connection;
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

    public void loggingEnabled(boolean loggingEnabled) {
        this.log = loggingEnabled;
    }
}