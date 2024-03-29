package de.MCmoderSD.utilities.database;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import de.MCmoderSD.UI.Frame;

import de.MCmoderSD.utilities.json.JsonNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.MCmoderSD.utilities.other.Calculate.*;

public class MySQL {

    // Associations
    private final Frame frame;

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
    private boolean noLog;

    // Constructor
    public MySQL(JsonNode databaseConfig, Frame frame, boolean noLog) {

        // Set Attributes
        host = databaseConfig.get("host").asText();
        port = databaseConfig.get("port").asInt();
        database = databaseConfig.get("database").asText();
        username = databaseConfig.get("username").asText();
        password = databaseConfig.get("password").asText();

        // Set Logging
        this.noLog = noLog;

        // Initialize Cache Lists
        channelCache = new HashMap<>();
        userCache = new HashMap<>();

        // Connect to database
        new Thread(this::connect).start();

        // Load Cache
        loadChannelCache();
        loadUserCache();

        // Set Frame
        this.frame = frame;
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
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkChannel(int id, String name) {

        if (channelCache.containsKey(id)) return;

        // New Thread
        new Thread(() -> {

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

                // Add to Cache
                channelCache.put(id, name);

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Checks Users
    @SuppressWarnings("JpaQueryApiInspection")
    private void checkUser(int id, String name) {

        // Return if in Cache
        if (userCache.containsKey(id)) return;

        // New Thread
        new Thread(() -> {

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

                // Add to Cache
                userCache.put(id, name);

            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    // Log Message
    public void logMessage(ChannelMessageEvent event) {

        // Set Variables
        var channelID = getChannelID(event);
        var userID = getUserID(event);
        String message = getMessage(event);

        // Update Frame
        if (frame != null) frame.log(MESSAGE, getChannel(event), getAuthor(event), message);

        // Return if logging is disabled
        if (noLog) return;

        new Thread(() -> {

            // Check Channel and User
            checkChannel(channelID, getChannel(event));
            checkUser(userID, getAuthor(event));

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

        // Update Frame
        if (frame != null) frame.log(COMMAND, getChannel(event), getAuthor(event), command);

        // Return if logging is disabled
        if (noLog) return;

        new Thread(() -> {

            // Set Variables
            var channelID = getChannelID(event);
            var userID = getUserID(event);

            // Check Channel and User
            checkChannel(channelID, getChannel(event));
            checkUser(userID, getAuthor(event));

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

        // Update Frame
        if (frame != null) frame.log(SYSTEM, getChannel(event), getAuthor(event), response);

        // Return if logging is disabled
        if (noLog) return;

        new Thread(() -> {

            // Set Variables
            var channelID = getChannelID(event);
            var userID = getUserID(event);

            // Check Channel and User
            checkChannel(channelID, getChannel(event));
            checkUser(userID, getAuthor(event));

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

        // Update Frame
        if (frame != null) frame.log(MESSAGE, channel, botName, message);

        // Return if logging is disabled
        if (noLog) return;

        new Thread(() -> {

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

    // Edit Blacklist
    @SuppressWarnings("JpaQueryApiInspection")
    public String editBlacklist(String channel, String command, boolean isBlocked) {

        // Check Channel
        if (!channelCache.containsValue(channel)) checkChannel(queryChannelID(channel), channel);

        // Edit Blacklist
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "SELECT blacklist FROM " + "channels" + " WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, channel);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return "Error: Channel not found";
            String blacklist = resultSet.getString("blacklist");
            if (blacklist == null) blacklist = "";
            ArrayList<String> list = new ArrayList<>(List.of(blacklist.split("; ")));
            if (isBlocked && !list.contains(command)) list.add(command);
            else if (!isBlocked) list.remove(command);
            list.remove("");

            // Prepare statement
            query = "UPDATE " + "channels" + " SET blacklist = ? WHERE name = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, String.join("; ", list));
            preparedStatement.setString(2, channel);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return (isBlocked ? "Blocking " : "Unblocking ") + command + " in " + channel;
    }

    // Edit Channel
    public String editChannel(String channel, boolean isActive) {

        // Edit Channel
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "channels" + " SET active = ? WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, isActive ? 1 : 0); // set active
            preparedStatement.setString(2, channel); // set channel
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return (isActive ? "Joining " : "Leaving ") + channel;
    }

    // Edit Command
    public String editCommand(ChannelMessageEvent event, String command, boolean isEnabled) {

        // Set Variables
        var channelID = getChannelID(event);
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Edit Command
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "CustomCommands" + " SET isEnabled = ? WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, isEnabled ? 1 : 0); // set isEnabled
            preparedStatement.setInt(2, channelID); // set channel
            preparedStatement.setString(3, command); // set command
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command " + (isEnabled ? "enabled" : "disabled");
    }

    // Edit Counter
    public String editCounter(ChannelMessageEvent event, String counter, int value) {

        // Set Variables
        var channelID = getChannelID(event);

        // Edit Counter
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "Counters" + " SET value = ? WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, value); // set value
            preparedStatement.setInt(2, channelID); // set channel
            preparedStatement.setString(3, counter); // set counter
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return counter + " counter: " + value;
    }

    // Delete Command
    public String deleteCommand(ChannelMessageEvent event, String command) {

        // Set Variables
        var channelID = getChannelID(event);
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Delete Command
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "CustomCommands" + " WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, command); // set command
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command removed";
    }

    // Create Command
    public String createCommand(ChannelMessageEvent event, String command, ArrayList<String> aliases, String commandResponse) {

        // Set Variables
        var channelID = getChannelID(event);
        String aliasesString = String.join("; ", aliases);

        // Prepare aliases
        aliasesString = aliasesString.isEmpty() ? "" : aliasesString;
        while (aliasesString.startsWith("; ")) aliasesString = aliasesString.substring(2);
        while (aliasesString.endsWith("; ")) aliasesString = aliasesString.trim();

        // Create Command
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "CustomCommands" + " (channel_id, command_name, command_alias, command_response, isEnabled) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, command); // set command
            preparedStatement.setString(3, aliasesString); // set aliases
            preparedStatement.setString(4, commandResponse); // set response
            preparedStatement.setBoolean(5, true); // set isEnabled
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }
        return command + " command created";
    }

    // Create Counter
    public String createCounter(ChannelMessageEvent event, String counter) {

        // Set Variables
        var channelID = getChannelID(event);

        // Create Counter
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "Counters" + " (channel_id, name, value) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, counter); // set counter
            preparedStatement.setInt(3, 0); // set value
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }
        return counter + " counter created";
    }

    // Delete Counter
    public String deleteCounter(ChannelMessageEvent event, String counter) {

        // Set Variables
        var channelID = getChannelID(event);

        // Delete Counter
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "Counters" + " WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, counter); // set counter
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return counter + " counter removed";
    }

    // Get Commands
    public ArrayList<String> getCommands(ChannelMessageEvent event, boolean all) {

        // Set Variables
        var channelID = getChannelID(event);
        ArrayList<String> commands = new ArrayList<>();

        // Get Commands
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "SELECT command_name, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? "" : " AND isEnabled = 1");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) commands.add(resultSet.getString("command_name"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return commands;
    }

    // Get Aliases
    public HashMap<String, String> getAliases(ChannelMessageEvent event, boolean all) {

        // Set Variables
        var channelID = getChannelID(event);
        HashMap<String, String> aliases = new HashMap<>();

        // Get Aliases
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "SELECT command_alias, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? "" : " AND isEnabled = 1");
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) aliases.put(resultSet.getString("command_alias"), resultSet.getString("command_name"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return aliases;
    }

    // Get Response
    public String getResponse(ChannelMessageEvent event, String command) {

        // Set Variables
        var channelID = getChannelID(event);
        String response = null;

        // Get Response
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "SELECT command_response FROM " + "CustomCommands" + " WHERE channel_id = ? AND command_name = ? AND isEnabled = 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, command); // set command
            ResultSet resultSet = preparedStatement.executeQuery();

            // Set Response
            if (resultSet.next()) response = resultSet.getString("command_response");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }

        return response;
    }

    // Get Active Channels
    public ArrayList<String> getActiveChannels() {

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

    // Get Blacklist
    public HashMap<String, ArrayList<String>> getBlacklist() {

        // Variables
        HashMap<String, ArrayList<String>> blacklist = new HashMap<>();
        HashMap<String, ArrayList<String>> tempList = new HashMap<>();

        // Get Blacklist
        try {
            if (!isConnected()) connect();

            // Get blacklist from database
            String query = "SELECT name, blacklist FROM " + "channels";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String channel = resultSet.getString("name");
                if (resultSet.getString("blacklist") == null) continue; // skip if null
                ArrayList<String> list = new ArrayList<>(List.of(resultSet.getString("blacklist").split("; ")));
                tempList.put(channel, list);
            }

            // Convert tempList to blacklist
            for (HashMap.Entry<String, ArrayList<String>> entry : tempList.entrySet()) {
                String channel = entry.getKey();
                ArrayList<String> commands = entry.getValue();
                for (String command : commands) {
                    if (!blacklist.containsKey(command)) blacklist.put(command, new ArrayList<>());
                    blacklist.get(command).add(channel);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return blacklist;
    }

    // Get Counters
    public HashMap<String, Integer> getCounters(ChannelMessageEvent event) {

        // Set Variables
        var channelID = getChannelID(event);
        HashMap<String, Integer> counters = new HashMap<>();

        // Get Counters
        try {
            if (!isConnected()) connect(); // connect

            // Prepare statement
            String query = "SELECT name, value FROM " + "Counters" + " WHERE channel_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) counters.put(resultSet.getString("name"), resultSet.getInt("value"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return counters;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public boolean isLoggingEnabled() {
        return noLog;
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

    @SuppressWarnings("unused")
    public void loggingEnabled(boolean loggingEnabled) {
        this.noLog = loggingEnabled;
    }
}