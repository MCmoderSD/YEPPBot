package de.MCmoderSD.utilities.database.manager;

import de.MCmoderSD.objects.TwitchMessageEvent;
import de.MCmoderSD.utilities.database.MySQL;

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

public class YEPPConnect {

    // Assosiations
    private final MySQL mySQL;

    // Constructor
    public YEPPConnect(MySQL mySQL) {

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

            // SQL statement for creating the lurk list table
            connection.prepareStatement(condition +
                    """
                    MinecraftWhitelist (
                    channel_id INT PRIMARY KEY,
                    whitelist TEXT,
                    user_pair TEXT,
                    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (channel_id) REFERENCES users(id)
                    )
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
        ArrayList<String> whitelist = getData(channelID, "whitelist") != null ? new ArrayList<>(Arrays.asList(Objects.requireNonNull(getData(channelID, "whitelist")).split(" "))) : new ArrayList<>();
        Set<String> temp = getData(channelID, "user_pair") != null ? new HashSet<>(Arrays.asList(Objects.requireNonNull(getData(channelID, "user_pair")).split(" - "))) : null;
        ArrayList<String> userPair = new ArrayList<>(temp != null ? temp : new ArrayList<>());
        ArrayList<String> users = new ArrayList<>();
        for (String id : userPair) users.add(id.split(" ")[0]);

        if (add) {
            if (whitelist.contains(mcUsername)) return "User is already whitelisted.";
            if (!users.contains(String.valueOf(userID))) {
                whitelist.add(mcUsername);
                userPair.add(userID + " " + mcUsername);
                updateWhitelist(channelID, whitelist, userPair);
                return "User added to whitelist.";
            } else {
                var index = users.indexOf(String.valueOf(userID));
                String oldName = userPair.get(index).split(" ")[1];
                whitelist.remove(oldName);
                whitelist.add(mcUsername);
                userPair.set(index, userID + " " + mcUsername);
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
                if (id.split(" ")[1].contains(mcUsername)) {
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
            if (!mySQL.isConnected()) mySQL.connect();
            PreparedStatement statement = mySQL.getConnection().prepareStatement("SELECT * FROM MinecraftWhitelist WHERE channel_id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return resultSet.getString(type);

            // Close resources
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            System.err.printf("Error while getting whitelist: %s\n", e.getMessage());
        }
        return null;
    }

    // Check if Channel exists in table
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean channelExists(int id) {
        try {
            if (!mySQL.isConnected()) mySQL.connect();
            PreparedStatement statement = mySQL.getConnection().prepareStatement("SELECT 1 FROM MinecraftWhitelist WHERE channel_id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
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
                if (!mySQL.isConnected()) mySQL.connect();
                PreparedStatement statement = mySQL.getConnection().prepareStatement("INSERT INTO MinecraftWhitelist (channel_id) VALUES (?)");
                statement.setInt(1, id);
                statement.executeUpdate();

                // Close resources
                statement.close();

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
            for (String name : whitelist) if (!(name.isEmpty() && name.isBlank())) whitelistString.append(name).append(" ");
            if (!whitelistString.isEmpty()) whitelistString.deleteCharAt(whitelistString.length() - 1);

            // Format userPair
            StringBuilder userPairString = new StringBuilder();
            for (String pair : userPair) if (!(pair.isEmpty() && pair.isBlank())) userPairString.append(pair).append(" - ");
            if (!userPairString.isEmpty()) userPairString.delete(userPairString.length() - 3, userPairString.length());

            try {
                if (!mySQL.isConnected()) mySQL.connect();
                PreparedStatement statement = mySQL.getConnection().prepareStatement("UPDATE MinecraftWhitelist SET whitelist = ?, user_pair = ?, last_updated = ? WHERE channel_id = ?");
                statement.setString(1, whitelistString.toString());
                statement.setString(2, userPairString.toString());
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.setInt(4, id);
                statement.executeUpdate();

                // Close resources
                statement.close();

            } catch (SQLException e) {
                System.err.printf("Error while updating whitelist: %s\n", e.getMessage());
            }
        }).start();
    }
}