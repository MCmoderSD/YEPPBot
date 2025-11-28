package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

public class ChannelManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public ChannelManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }

    private void addChannel(TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

                // Add Twitch User to database
                database.addTwitchUser(channel);

                // Insert channel
                PreparedStatement insertChannelStatement = connection.prepareStatement(
                        "INSERT IGNORE INTO Channel (id) VALUES (?)"
                );

                // Set the insert values
                insertChannelStatement.setInt(1, channel.getId());  // Channel ID

                // Execute the statement
                insertChannelStatement.executeUpdate();

                // Close the statement
                insertChannelStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add channel: " + e.getMessage(), e);
            }
        }).start();
    }

    private void setActive(TwitchUser channel, boolean active) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

                // Add Channel to database
                addChannel(channel);

                // Update channel active status
                PreparedStatement updateChannelStatement = connection.prepareStatement(
                        "UPDATE Channel SET active = ? WHERE id = ?"
                );

                // Set the update values
                updateChannelStatement.setBoolean(1, active);       // Active flag
                updateChannelStatement.setInt(2, channel.getId());  // Channel ID

                // Execute the statement
                updateChannelStatement.executeUpdate();

                // Close the statement
                updateChannelStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to set channel active status: " + e.getMessage(), e);
            }
        }).start();
    }

    public void joinChannel(TwitchUser channel) {
        setActive(channel, true);
    }

    public void leaveChannel(TwitchUser channel) {
        setActive(channel, false);
    }

    public HashMap<TwitchUser, Boolean> getChannels() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT user, active FROM User u, Channel c WHERE u.id = c.id"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<TwitchUser, Boolean> activeChannels = new HashMap<>();

            // Process results
            while (resultSet.next()) activeChannels.put(inflateTwitchUser(resultSet.getBytes("user")), resultSet.getBoolean("active"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return result
            return activeChannels;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get active channels: " + e.getMessage(), e);
        }
    }

    public void setAutoShoutout(TwitchUser channel, boolean autoShoutout) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

                // Add Channel to database
                addChannel(channel);

                // Update channel auto shoutout status
                PreparedStatement updateChannelStatement = connection.prepareStatement(
                        "UPDATE Channel SET autoShoutout = ? WHERE id = ?"
                );

                // Set the update values
                updateChannelStatement.setBoolean(1, autoShoutout); // Auto Shout
                updateChannelStatement.setInt(2, channel.getId());  // Channel ID

                // Execute the statement
                updateChannelStatement.executeUpdate();

                // Close the statement
                updateChannelStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to set auto shoutout: " + e.getMessage(), e);
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public void enableAutoShoutout(TwitchUser channel) {
        setAutoShoutout(channel, true);
    }

    @SuppressWarnings("unused")
    public void disableAutoShoutout(TwitchUser channel) {
        setAutoShoutout(channel, false);
    }

    public HashMap<Integer, Boolean> getAutoShoutoutChannels() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, autoShoutout FROM Channel"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<Integer, Boolean> autoShoutoutChannels = new HashMap<>();

            // Process results
            while (resultSet.next()) autoShoutoutChannels.put(resultSet.getInt("id"), resultSet.getBoolean("autoShoutout"));

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return result
            return autoShoutoutChannels;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check auto shoutout: " + e.getMessage(), e);
        }
    }

    public HashMap<Integer, HashSet<String>> blacklistAdd(TwitchUser channel, String command) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

            // Add Channel to database
            addChannel(channel);

            // Insert blacklist entry
            PreparedStatement insertBlacklistStatement = connection.prepareStatement(
                    "INSERT IGNORE INTO Blacklist (id, command) VALUES (?, ?)"
            );

            // Set the insert values
            insertBlacklistStatement.setInt(1, channel.getId());            // Channel ID
            insertBlacklistStatement.setString(2, command.toLowerCase());   // Command

            // Execute the statement
            insertBlacklistStatement.executeUpdate();

            // Close the statement
            insertBlacklistStatement.close();

            // Return updated blacklist
            return getBlacklist();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add to blacklist: " + e.getMessage(), e);
        }
    }

    public HashMap<Integer, HashSet<String>> blacklistRemove(TwitchUser channel, String command) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

            // Add Channel to database
            addChannel(channel);

            // Delete blacklist entry
            PreparedStatement deleteBlacklistStatement = connection.prepareStatement(
                    "DELETE IGNORE FROM Blacklist WHERE id = ? AND command = ?"
            );

            // Set the delete values
            deleteBlacklistStatement.setInt(1, channel.getId());            // Channel ID
            deleteBlacklistStatement.setString(2, command.toLowerCase());   // Command

            // Execute the statement
            deleteBlacklistStatement.executeUpdate();

            // Close the statement
            deleteBlacklistStatement.close();

            // Return updated blacklist
            return getBlacklist();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove from blacklist: " + e.getMessage(), e);
        }
    }

    public HashMap<Integer, HashSet<String>> getBlacklist() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT id, command FROM Blacklist"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<Integer, HashSet<String>> blacklist = new HashMap<>();

            // Process results
            while (resultSet.next()) {
                var id = resultSet.getInt("id");
                String command = resultSet.getString("command");
                blacklist.putIfAbsent(id, new HashSet<>());
                blacklist.get(id).add(command);
            }

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return result
            return blacklist;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get blacklist: " + e.getMessage(), e);
        }
    }
}