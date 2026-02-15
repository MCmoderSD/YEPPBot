package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

@SuppressWarnings("unused")
public class ChannelManager {

    // Associations
    private final Database database;

    // Constructor
    public ChannelManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    // Add Channel Method
    private void addChannel(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Add Twitch User to database
            database.addTwitchUser(channel);

            // Insert channel
            PreparedStatement insertChannelStatement = database.getConnection().prepareStatement(
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
    }

    // Set Channel Active Method
    public void setActive(TwitchUser channel, boolean active) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Add Channel to database
            addChannel(channel);

            // Update channel active status
            PreparedStatement updateChannelStatement = database.getConnection().prepareStatement(
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
    }

    // Convenience Methods
    public void joinChannel(TwitchUser channel) {
        setActive(channel, true);
    }

    public void leaveChannel(TwitchUser channel) {
        setActive(channel, false);
    }

    // Get Active Channels Method
    public HashMap<TwitchUser, Boolean> getChannels() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(
                    "SELECT user, active FROM User u, Channel c WHERE u.id = c.id"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<TwitchUser, Boolean> activeChannels = new HashMap<>();

            // Process results
            while (resultSet.next()) activeChannels.put(
                    inflateTwitchUser(resultSet.getBytes("user")),  // Twitch User
                    resultSet.getBoolean("active")                  // Active flag
            );

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return result
            return activeChannels;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get active channels: " + e.getMessage(), e);
        }
    }

    // Set Auto Shoutout Method
    public void setAutoShoutout(TwitchUser channel, boolean autoShoutout) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

                // Add Channel to database
                addChannel(channel);

                // Update channel auto shoutout status
                PreparedStatement updateChannelStatement = database.getConnection().prepareStatement(
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

    // Convenience Methods
    public void enableAutoShoutout(TwitchUser channel) {
        setAutoShoutout(channel, true);
    }

    public void disableAutoShoutout(TwitchUser channel) {
        setAutoShoutout(channel, false);
    }

    // Get Auto Shoutout Channels Method
    public HashMap<TwitchUser, Boolean> getAutoShoutoutChannels() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(
                    "SELECT user, autoShoutout FROM Channel JOIN User ON Channel.id = User.id;"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<TwitchUser, Boolean> autoShoutoutChannels = new HashMap<>();

            // Process results
            while (resultSet.next()) autoShoutoutChannels.put(
                    inflateTwitchUser(resultSet.getBytes("user")),  // Twitch User
                    resultSet.getBoolean("autoShoutout")            // Auto Shoutout flag
            );

            // Close resources
            resultSet.close();
            preparedStatement.close();

            // Return result
            return autoShoutoutChannels;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check auto shoutout: " + e.getMessage(), e);
        }
    }

    // Add Command to Blacklist
    public HashMap<TwitchUser, HashSet<String>> blacklistAdd(TwitchUser channel, String command) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

            // Add Channel to database
            addChannel(channel);

            // Insert blacklist entry
            PreparedStatement insertBlacklistStatement = database.getConnection().prepareStatement(
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

    // Remove Command from Blacklist
    public HashMap<TwitchUser, HashSet<String>> blacklistRemove(TwitchUser channel, String command) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (command == null || command.isBlank()) throw new IllegalArgumentException("Command cannot be null or blank");

            // Add Channel to database
            addChannel(channel);

            // Delete blacklist entry
            PreparedStatement deleteBlacklistStatement = database.getConnection().prepareStatement(
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

    // Get Blacklist Method
    public HashMap<TwitchUser, HashSet<String>> getBlacklist() {
        try {

            // Prepare statement
            PreparedStatement preparedStatement = database.getConnection().prepareStatement(
                    "SELECT user, command FROM Blacklist JOIN User ON Blacklist.id = User.id"
            );

            // Execute query
            var resultSet = preparedStatement.executeQuery();

            // Prepare result map
            HashMap<TwitchUser, HashSet<String>> blacklist = new HashMap<>();

            // Process results
            while (resultSet.next()) {
                TwitchUser user = inflateTwitchUser(resultSet.getBytes("user"));    // Twitch User
                String command = resultSet.getString("command");                    // Command
                blacklist.putIfAbsent(user, new HashSet<>());
                blacklist.get(user).add(command);
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