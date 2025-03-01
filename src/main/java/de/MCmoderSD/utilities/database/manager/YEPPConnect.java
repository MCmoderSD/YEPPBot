package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;

import static de.MCmoderSD.utilities.other.Format.*;

public class YEPPConnect {

    // Associations
    private final SQL sql;

    // Constructor
    public YEPPConnect(SQL sql) {

        // Set associations
        this.sql = sql;

        // Initialize
        initTables();
    }

    // Initialize Tables
    private void initTables() {
        try {

            // Variables
            Connection connection = sql.getConnection();

            // Condition for creating tables
            String condition = "CREATE TABLE IF NOT EXISTS ";

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                    """
                    MinecraftWhitelist (
                    channelId INT PRIMARY KEY,
                    whitelist TEXT,
                    userPair TEXT,
                    lastUpdate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (channelId) REFERENCES Users(id)
                    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=1 CHARSET=utf8mb4
                    """
            ).execute();
          
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public String editWhitelist(TwitchMessageEvent event, String mcUsername, boolean add) {

        // Variables
        var channelID = event.getChannelId();
        var userID = event.getUserId();

        // Check if Channel exists
        if (!add && !channelExists(channelID)) return "No whitelist found for this channel.";
        if (add && !channelExists(channelID)) createChannel(channelID);

        // Get Whitelist
        ArrayList<String> whitelist = getData(channelID, "whitelist") != null ? new ArrayList<>(Arrays.asList(Objects.requireNonNull(getData(channelID, "whitelist")).split(SPACE))) : new ArrayList<>();
        Set<String> temp = getData(channelID, "userPair") != null ? new HashSet<>(Arrays.asList(Objects.requireNonNull(getData(channelID, "userPair")).split(" - "))) : null;
        ArrayList<String> userPair = new ArrayList<>(temp != null ? temp : new ArrayList<>());
        ArrayList<String> users = new ArrayList<>();
        for (String id : userPair) users.add(id.split(SPACE)[0]);

        if (add) {
            if (whitelist.contains(mcUsername)) return "User is already whitelisted.";
            if (!users.contains(String.valueOf(userID))) {
                whitelist.add(mcUsername);
                userPair.add(userID + SPACE + mcUsername);
                updateWhitelist(channelID, whitelist, userPair);
                return "User added to whitelist.";
            } else {
                var index = users.indexOf(String.valueOf(userID));
                String oldName = userPair.get(index).split(SPACE)[1];
                whitelist.remove(oldName);
                whitelist.add(mcUsername);
                userPair.set(index, userID + SPACE + mcUsername);
                updateWhitelist(channelID, whitelist, userPair);
                return "User updated in whitelist.";
            }
        } else {
            if (!whitelist.contains(mcUsername)) return "User is not whitelisted.";

            // Remove user from whitelist
            whitelist.remove(mcUsername);

            // Remove user from userPair
            var index = 0;
            for (String id : userPair) {
                if (id.split(SPACE)[1].contains(mcUsername)) {
                    userPair.remove(index);
                    break;
                }
                index++;
            }

            updateWhitelist(channelID, whitelist, userPair);
            return "User removed from whitelist.";
        }
    }

    // Get List
    private String getData(int id, String type) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "SELECT * FROM MinecraftWhitelist WHERE channelId = ?"
            );

            // Set values and execute
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString(type);

            // Close resources
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.printf("Error while getting whitelist: %s\n", e.getMessage());
        }
        return null;
    }

    // Check if Channel exists in table
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean channelExists(int id) {
        try {
            if (!sql.isConnected()) sql.connect();

            // Prepare statement
            PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                    "SELECT 1 FROM MinecraftWhitelist WHERE channelId = ?"
            );

            // Set values and execute
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            System.err.printf("Error while checking if channel exists: %s\n", e.getMessage());
        }
        return false;
    }

    // Create Channel
    private void createChannel(int id) {
        new Thread(() -> {
            try {
                if (!sql.isConnected()) sql.connect();

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "INSERT INTO MinecraftWhitelist (channelId) VALUES (?)"
                );

                // Set values and execute
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.printf("Error while creating channel: %s\n", e.getMessage());
            }
        }).start();
    }

    // Update Whitelist
    private void updateWhitelist(int id, ArrayList<String> whitelist, ArrayList<String> userPair) {
        new Thread(() -> {

            // Format whitelist
            StringBuilder whitelistString = new StringBuilder();
            for (String name : whitelist) if (!name.isEmpty()) whitelistString.append(name).append(SPACE);
            if (!whitelistString.isEmpty()) whitelistString.deleteCharAt(whitelistString.length() - 1);

            // Format userPair
            StringBuilder userPairString = new StringBuilder();
            for (String pair : userPair) if (!pair.isEmpty()) userPairString.append(pair).append(" - ");
            if (!userPairString.isEmpty()) userPairString.delete(userPairString.length() - 3, userPairString.length());

            try {
                if (!sql.isConnected()) sql.connect();

                // Prepare statement
                PreparedStatement preparedStatement = sql.getConnection().prepareStatement(
                        "UPDATE MinecraftWhitelist SET whitelist = ?, userPair = ?, lastUpdate = ? WHERE channelId = ?"
                );

                // Set values and execute
                preparedStatement.setString(1, whitelistString.toString());
                preparedStatement.setString(2, userPairString.toString());
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.setInt(4, id);
                preparedStatement.executeUpdate();

                // Close resources
                preparedStatement.close();

            } catch (SQLException e) {
                System.err.printf("Error while updating whitelist: %s\n", e.getMessage());
            }
        }).start();
    }
}