package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomManager {

    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public CustomManager(MySQL mySQL) {

        // Set associations
        this.mySQL = mySQL;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = mySQL.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

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
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Get CustomCommands
    public HashMap<Integer, HashMap<String, String>> getCustomCommands() {

        // Variables
        HashMap<Integer, HashMap<String, String>> customCommands = new HashMap<>();

        // Get CustomCommands
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT channel_id, command_name, command_response FROM CustomCommands WHERE isEnabled = 1";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process results
            while (resultSet.next()) {
                int channelId = resultSet.getInt("channel_id");
                String commandName = resultSet.getString("command_name");
                String commandResponse = resultSet.getString("command_response");

                // Add to customCommands
                customCommands
                        .computeIfAbsent(channelId, k -> new HashMap<>())
                        .put(commandName, commandResponse);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customCommands;
    }

    // Get CustomAliases
    public HashMap<Integer, HashMap<String, String>> getCustomAliases() {

        // Variables
        HashMap<Integer, HashMap<String, String>> customAliases = new HashMap<>();

        // Get CustomAliases
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT channel_id, command_alias, command_name FROM CustomCommands WHERE isEnabled = 1";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process results
            while (resultSet.next()) {
                int channelId = resultSet.getInt("channel_id");
                String commandAlias = resultSet.getString("command_alias");
                String commandName = resultSet.getString("command_name");

                // Split aliases and add to customAliases
                if (commandAlias != null) {
                    String[] aliases = commandAlias.split("; ");
                    for (String alias : aliases) {
                        customAliases
                                .computeIfAbsent(channelId, k -> new HashMap<>())
                                .put(alias, commandName);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customAliases;
    }

    // Get Commands
    public ArrayList<String> getCommands(TwitchMessageEvent event, boolean all) {

        // Set Variables
        var channelID = event.getChannelId();
        ArrayList<String> commands = new ArrayList<>();

        // Get Commands
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT command_name, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? "" : " AND isEnabled = 1");
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) commands.add(resultSet.getString("command_name"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return commands;
    }

    // Get Aliases
    public HashMap<String, String> getAliases(TwitchMessageEvent event, boolean all) {

        // Set Variables
        var channelID = event.getChannelId();
        HashMap<String, String> aliases = new HashMap<>();

        // Get Aliases
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT command_alias, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? "" : " AND isEnabled = 1");
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {
                var alias = resultSet.getString("command_alias");
                var command = resultSet.getString("command_name");
                if (alias == null) continue;

                String[] aliasList = alias.split("; ");
                for (String name : aliasList) aliases.put(name, command);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return aliases;
    }

    // Create Command
    public String createCommand(TwitchMessageEvent event, String command, ArrayList<String> aliases, String commandResponse) {

        // Set Variables
        var channelID = event.getChannelId();
        String aliasesString = null;
        if (!aliases.isEmpty()) aliasesString = String.join("; ", aliases);

        // Prepare aliases
        if (aliasesString != null && !aliasesString.isEmpty()) {
            while (aliasesString.startsWith("; ")) aliasesString = aliasesString.substring(2);
            while (aliasesString.endsWith("; ")) aliasesString = aliasesString.trim();
        }

        // Create Command
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "CustomCommands" + " (channel_id, command_name, command_alias, command_response, isEnabled) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
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

    // Edit Command
    public String editCommand(TwitchMessageEvent event, String command, boolean enable) {

        // Set Variables
        var channelID = event.getChannelId();
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Edit Command
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "CustomCommands" + " SET isEnabled = ? WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, enable ? 1 : 0); // set isEnabled
            preparedStatement.setInt(2, channelID); // set channel
            preparedStatement.setString(3, command); // set command
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command " + (enable ? "enabled" : "disabled");
    }

    // Delete Command
    public String deleteCommand(TwitchMessageEvent event, String command) {

        // Set Variables
        var channelID = event.getChannelId();
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Delete Command
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "CustomCommands" + " WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, channelID); // set channel
            preparedStatement.setString(2, command); // set command
            preparedStatement.executeUpdate(); // execute
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command removed";
    }
}