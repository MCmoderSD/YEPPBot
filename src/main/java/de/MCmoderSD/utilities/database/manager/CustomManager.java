package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.core.BotClient;
import de.MCmoderSD.objects.Timer;
import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static de.MCmoderSD.utilities.other.Format.*;

public class CustomManager {

    // Associations
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
                    FOREIGN KEY (channel_id) REFERENCES users(id)
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
                    FOREIGN KEY (channel_id) REFERENCES users(id)
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
                    FOREIGN KEY (channel_id) REFERENCES users(id)
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

            // Close resources
            resultSet.close();
            preparedStatement.close();

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

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customAliases;
    }

    // Get Commands
    public ArrayList<String> getCommands(TwitchMessageEvent event, boolean all) {

        // Set Variables
        ArrayList<String> commands = new ArrayList<>();

        // Get Commands
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT command_name, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? EMPTY : " AND isEnabled = 1");
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) commands.add(resultSet.getString("command_name"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return commands;
    }

    // Get Aliases
    public HashMap<String, String> getAliases(TwitchMessageEvent event, boolean all) {

        // Set Variables
        HashMap<String, String> aliases = new HashMap<>();

        // Get Aliases
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT command_alias, command_name FROM " + "CustomCommands" + " WHERE channel_id = ?" + (all ? EMPTY : " AND isEnabled = 1");
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {
                var alias = resultSet.getString("command_alias");
                var command = resultSet.getString("command_name");
                if (alias == null) continue;

                String[] aliasList = alias.split("; ");
                for (String name : aliasList) aliases.put(name, command);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return aliases;
    }

    // Create Command
    public String createCommand(TwitchMessageEvent event, String command, ArrayList<String> aliases, String commandResponse) {

        // Set Variables
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
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, command); // set command
            preparedStatement.setString(3, aliasesString); // set aliases
            preparedStatement.setString(4, commandResponse); // set response
            preparedStatement.setBoolean(5, true); // set isEnabled
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }
        return command + " command created";
    }

    // Edit Command
    public String editCommand(TwitchMessageEvent event, String command, boolean enable) {

        // Set Variables
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Edit Command
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "CustomCommands" + " SET isEnabled = ? WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, enable ? 1 : 0); // set isEnabled
            preparedStatement.setInt(2, event.getChannelId()); // set channel
            preparedStatement.setString(3, command); // set command
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command " + (enable ? "enabled" : "disabled");
    }

    // Delete Command
    public String deleteCommand(TwitchMessageEvent event, String command) {

        // Set Variables
        if (!getCommands(event, true).contains(command)) command = getAliases(event, true).get(command);

        // Delete Command
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "CustomCommands" + " WHERE channel_id = ? AND command_name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, command); // set command
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return command + " command removed";
    }

    // Get CustomCounters
    public HashMap<Integer, HashMap<String, Integer>> getCustomCounters() {

        // Variables
        HashMap<Integer, HashMap<String, Integer>> customCounters = new HashMap<>();

        // Get CustomCounters
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT channel_id, name, value FROM Counters";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process results
            while (resultSet.next()) {
                int channelId = resultSet.getInt("channel_id");
                String counterName = resultSet.getString("name");
                int counterValue = resultSet.getInt("value");

                // Add to customCounters
                customCounters
                        .computeIfAbsent(channelId, k -> new HashMap<>())
                        .put(counterName, counterValue);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customCounters;
    }

    // Get Counters
    public HashMap<String, Integer> getCounters(TwitchMessageEvent event) {

        // Set Variables
        HashMap<String, Integer> counters = new HashMap<>();

        // Get Counters
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT name, value FROM " + "Counters" + " WHERE channel_id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) counters.put(resultSet.getString("name"), resultSet.getInt("value"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return counters;
    }

    // Create Counter
    public String createCounter(TwitchMessageEvent event, String counter) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "Counters" + " (channel_id, name, value) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, counter); // set counter
            preparedStatement.setInt(3, 0); // set value
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }
        return counter + " counter created";
    }

    // Edit Counter
    public String editCounter(TwitchMessageEvent event, String counter, int value) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "Counters" + " SET value = ? WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, value); // set value
            preparedStatement.setInt(2, event.getChannelId()); // set channel
            preparedStatement.setString(3, counter); // set counter
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return counter + " counter: " + value;
    }

    // Delete Counter
    public String deleteCounter(TwitchMessageEvent event, String counter) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "Counters" + " WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, counter); // set counter
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return counter + " counter removed";
    }

    // Get All Custom Timers
    public HashMap<Integer, HashSet<Timer>> getCustomTimers(BotClient botClient) {

        // Variables
        HashMap<Integer, HashSet<Timer>> customTimers = new HashMap<>();

        // Get CustomTimer
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT channel_id, name, time, response FROM CustomTimers WHERE isEnabled = 1";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process results
            while (resultSet.next()) {
                int channelId = resultSet.getInt("channel_id");
                String name = resultSet.getString("name");
                String time = resultSet.getString("time");
                String response = resultSet.getString("response");

                // Create Timer object
                Timer timer = new Timer(botClient, botClient.getHelixHandler().getUser(channelId).getName(), name, time, response);

                // Add to customTimers
                customTimers.computeIfAbsent(channelId, k -> new HashSet<>(Set.of(timer)));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customTimers;
    }

    // Get Custom Timers
    public HashMap<String, Timer> getCustomTimers(TwitchMessageEvent event, BotClient botClient) {

        // Set Variables
        HashMap<String, Timer> customTimers = new HashMap<>();

        // Get Custom Timers
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT name, time, response FROM " + "CustomTimers" + " WHERE channel_id = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String time = resultSet.getString("time");
                String response = resultSet.getString("response");
                customTimers.put(name, new Timer(botClient, event.getChannel(), name, time, response));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return customTimers;
    }

    // Get Active Custom Commands
    public HashSet<Timer> getActiveCustomTimers(TwitchMessageEvent event, BotClient botClient) {

        // Variables
        HashSet<Timer> customTimers = new HashSet<>();

        // Get Custom Timers
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "SELECT channel_id, name, time, response FROM " + "CustomTimers WHERE isEnabled = 1";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Add to List
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String time = resultSet.getString("time");
                String response = resultSet.getString("response");
                customTimers.add(new Timer(botClient, event.getChannel(), name, time, response));
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return customTimers;
    }

    // Create Custom Timer
    public String createCustomTimer(TwitchMessageEvent event, String name, String time, String response) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "INSERT INTO " + "CustomTimers" + " (channel_id, name, time, response) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, name); // set name
            preparedStatement.setString(3, time); // set time
            preparedStatement.setString(4, response); // set response
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }
        return name + " custom timer created";
    }

    // Edit Custom Timer
    public String editCustomTimer(TwitchMessageEvent event, String name, String time, String response, boolean enable) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "UPDATE " + "CustomTimers" + " SET time = ?, response = ?, isEnabled = ? WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setString(1, time); // set time
            preparedStatement.setString(2, response); // set response
            preparedStatement.setInt(3, enable ? 1 : 0); // set isEnabled
            preparedStatement.setInt(4, event.getChannelId()); // set channel
            preparedStatement.setString(5, name); // set name
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return name + " custom timer " + (enable ? "enabled" : "disabled");
    }

    // Delete Custom Timer
    public String deleteCustomTimer(TwitchMessageEvent event, String name) {
        try {
            if (!mySQL.isConnected()) mySQL.connect(); // connect

            // Prepare statement
            String query = "DELETE FROM " + "CustomTimers" + " WHERE channel_id = ? AND name = ?";
            PreparedStatement preparedStatement = mySQL.getConnection().prepareStatement(query);
            preparedStatement.setInt(1, event.getChannelId()); // set channel
            preparedStatement.setString(2, name); // set name
            preparedStatement.executeUpdate(); // execute

            // Close resources
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Error: Database error";
        }

        return name + " custom timer removed";
    }
}