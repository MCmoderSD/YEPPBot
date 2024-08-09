package de.MCmoderSD.utilities.database;

import de.MCmoderSD.main.Main;
import de.MCmoderSD.objects.TwitchMessageEvent;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.MCmoderSD.utilities.other.Calculate.*;

@SuppressWarnings("unused")
public class MySQL extends Driver {

    // Cache Lists
    private final HashMap<Integer, String> channelCache;
    private final HashMap<Integer, String> userCache;

    // Variables
    private boolean log;

    // Constructor
    public MySQL(Main main) {

        // Initialize Driver
        super(main.getCredentials().getMySQLConfig());

        // Set Logging
        log = main.hasArg("log");

        // Connect to database
        new Thread(this::connect).start();
        initTables();

        // Load Cache
        channelCache = loadCache("channels");
        userCache = loadCache("users");
    }

    private void initTables() {
        try {
            if (!isConnected()) connect();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the users table
            connection.prepareStatement(condition +
                            """
                            users (
                            id INT PRIMARY KEY,
                            name VARCHAR(25) NOT NULL
                            )
                            """
            ).execute();

            // SQL statement for creating the channels table
            connection.prepareStatement(condition +
                            """
                            channels (
                            id INT PRIMARY KEY,
                            name VARCHAR(25) NOT NULL,
                            blacklist TEXT,
                            active BIT NOT NULL DEFAULT 1
                            )
                            """
            ).execute();

            // SQL statement for creating the message log table
            connection.prepareStatement(condition +
                            """
                            MessageLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            type VARCHAR(5) NOT NULL,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            message VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the command log table
            connection.prepareStatement(condition +
                            """
                            CommandLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            command TEXT NOT NULL,
                            args VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the response log table
            connection.prepareStatement(condition +
                            """
                            ResponseLog (
                            timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            channel_id INT NOT NULL,
                            user_id INT NOT NULL,
                            command TEXT NOT NULL,
                            args VARCHAR(500),
                            response VARCHAR(500),
                            bits INT NOT NULL DEFAULT 0,
                            subMonths INT NOT NULL DEFAULT 0,
                            subStreak INT NOT NULL DEFAULT 0,
                            subPlan VARCHAR(5) NOT NULL DEFAULT 'NONE',
                            FOREIGN KEY (channel_id) REFERENCES channels(id),
                            FOREIGN KEY (user_id) REFERENCES users(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                            """
                            lurkList (
                            user_id INT PRIMARY KEY,
                            lurkChannel_ID INT NOT NULL,
                            startTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            traitorChannel TEXT,
                            FOREIGN KEY (lurkChannel_ID) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the custom timers table
            connection.prepareStatement(condition +
                            """
                            CustomTimers (
                            channel_id INT NOT NULL,
                            name TEXT NOT NULL,
                            time TEXT NOT NULL,
                            response VARCHAR(500) NOT NULL,
                            isEnabled BIT NOT NULL DEFAULT 1,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the custom commands table
            connection.prepareStatement(condition +
                            """
                            CustomCommands (
                            channel_id INT NOT NULL,
                            command_name TEXT NOT NULL,
                            command_alias TEXT,
                            command_response VARCHAR(500) NOT NULL,
                            isEnabled BIT NOT NULL DEFAULT 1,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the counters table
            connection.prepareStatement(condition +
                            """
                            Counters (
                            channel_id INT NOT NULL,
                            name TEXT NOT NULL,
                            value INT NOT NULL,
                            FOREIGN KEY (channel_id) REFERENCES channels(id)
                            )
                            """
            ).execute();

            // SQL statement for creating the fact list table
            connection.prepareStatement(condition +
                            """
                            factList (
                            fact_id INT PRIMARY KEY AUTO_INCREMENT,
                            en_percent varchar(500),
                            en_people varchar(500),
                            en_verb varchar(500),
                            en_frequency varchar(500),
                            en_adjective varchar(500),
                            en_object varchar(500),
                            de_percent varchar(500),
                            de_people varchar(500),
                            de_verb varchar(500),
                            de_frequency varchar(500),
                            de_adjective varchar(500),
                            de_object varchar(500)
                            )
                            """
            ).execute();

            // SQL statement for creating the joke list table
            connection.prepareStatement(condition +
                            """
                            jokeList (
                            joke_id INT PRIMARY KEY AUTO_INCREMENT,
                            en varchar(500),
                            de varchar(500)
                            )
                            """
            ).execute();

            // SQL statement for creating the insult list table
            connection.prepareStatement(condition +
                            """
                            insultList (
                            insult_id INT PRIMARY KEY AUTO_INCREMENT,
                            en varchar(500) NOT NULL,
                            de varchar(500) NOT NULL
                            )
                            """
            ).execute();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Load Cache
    private HashMap<Integer, String> loadCache(String table) {

        // Variables
        HashMap<Integer, String> cache = new HashMap<>();

        // Load Channel Cache
        try {
            if (!isConnected()) connect(); // connect

            String query = "SELECT * FROM " + table;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) cache.put(resultSet.getInt("id"), resultSet.getString("name")); // add to cache
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return cache;
    }

    // Update Channels and Users
    private void updateCache(int id, String name, String table) throws SQLException {
        if (!isConnected()) connect(); // connect

        // Check Channel
        String selectQuery = "SELECT * FROM " + table + " WHERE id = ?";
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

        // Close resources
        resultSet.close();
        selectPreparedStatement.close(); // close the selectPreparedStatement
    }

    // Check Cache
    public void checkCache(int id, String name) throws SQLException {

        // Check Cache
        boolean channel = channelCache.containsKey(id) && channelCache.get(id).equals(name);
        boolean user = userCache.containsKey(id) && userCache.get(id).equals(name);

        // Return if in Cache
        if (channel && user) return;

        // Update Channel Cache
        if (!channel) {
            updateCache(id, name, "channels");
            channelCache.put(id, name);
        }

        // Update User Cache
        if (!user) {
            updateCache(id, name, "users");
            userCache.put(id, name);
        }
    }

    // Log Message
    public void logMessage(TwitchMessageEvent event) {
        if (log) new Thread(() -> {
            event.logToMySQL(this); // log to MySQL
        }).start();
    }

    // Log Command Response
    public void logResponse(TwitchMessageEvent event, String command, String response) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!isConnected()) connect(); // connect

                // Check Channel and User
                checkCache(channelID, channel);
                checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response, bits, subMonths, subStreak, subPlan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, event.getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, command); // set command
                preparedStatement.setString(5, event.getMessage()); // set args
                preparedStatement.setString(6, response); // set response
                preparedStatement.setInt(7, event.getBits()); // set bits
                preparedStatement.setInt(8, event.getSubMonths()); // set subMonths
                preparedStatement.setInt(9, event.getSubStreak()); // set subStreak
                preparedStatement.setString(10, event.getSubTier()); // set subPlan
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Bot Response
    public void logResponse(String channel, String user, String message) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = queryID("channels", channel);
            var userID = queryID("users", user);

            try {
                if (!isConnected()) connect(); // connect

                // Check Channel and User
                checkCache(channelID, channel);
                checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "ResponseLog" + " (timestamp, channel_id, user_id, command, args, response) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, USER); // set command
                preparedStatement.setString(5, USER); // set args
                preparedStatement.setString(6, message); // set response
                preparedStatement.executeUpdate(); // execute
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Command
    public void logCommand(TwitchMessageEvent event, String trigger, String args) {
        if (log) new Thread(() -> {

            // Variables
            var channelID = event.getChannelId();
            var userID = event.getUserId();
            var channel = event.getChannel();
            var user = event.getUser();

            try {
                if (!isConnected()) connect(); // connect

                // Check Channel and User
                checkCache(channelID, channel);
                checkCache(userID, user);

                // Prepare statement
                String query = "INSERT INTO " + "CommandLog" + " (timestamp, channel_id, user_id, command, args, bits, subMonths, subStreak, subPlan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, event.getTimestamp()); // set timestamp
                preparedStatement.setInt(2, channelID); // set channel
                preparedStatement.setInt(3, userID); // set user
                preparedStatement.setString(4, trigger); // set command
                preparedStatement.setString(5, args); // set args
                preparedStatement.setInt(6, event.getBits()); // set bits
                preparedStatement.setInt(7, event.getSubMonths()); // set subMonths
                preparedStatement.setInt(8, event.getSubStreak()); // set subStreak
                preparedStatement.setString(9, event.getSubTier()); // set subPlan
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

    // Get Black List
    public HashMap<Integer, ArrayList<String>> getBlackList() {

        // Variables
        HashMap<Integer, ArrayList<String>> blackList = new HashMap<>();

        try {
            if (!isConnected()) connect();

            String query = "SELECT id, blacklist FROM " + "channels";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String blacklist = resultSet.getString("blacklist");
                if (blacklist != null) blackList.put(id, new ArrayList<>(List.of(blacklist.toLowerCase().split("; "))));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return blackList;
    }

    // Get Lurk List
    // ToDo Implement

    // Query ID
    public int queryID(String table, String name) {
        try {
            if (!isConnected()) connect();
            String query = null;
            if (table.equals("channels")) query = "SELECT id FROM " + "channels" + " WHERE name = ?";
            if (table.equals("users")) query = "SELECT id FROM " + "users" + " WHERE name = ?";
            if (query == null) return -1;
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt("id");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return -1;
    }

    // Query Name
    public String queryName(String table, int id) {

        try {
            if (!isConnected()) connect();
            String query = null;
            if (table.equals("channels")) query = "SELECT name FROM " + "channels" + " WHERE id = ?";
            if (table.equals("users")) query = "SELECT name FROM " + "users" + " WHERE id = ?";
            if (query == null) return null;
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
    public boolean isLoggingEnabled() {
        return log;
    }

    // Setter
    public void loggingEnabled(boolean loggingEnabled) {
        this.log = loggingEnabled;
    }
}