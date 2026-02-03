package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

public class QueueManager {

    // Associations
    private final Database database;

    // Constructor
    public QueueManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    public void enqueueUser(MessageEvent event) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent event cannot be null");

                // Variables
                TwitchUser user = event.getUser();
                TwitchUser channel = event.getChannel();
                Timestamp joinedAt = Timestamp.from(event.getFiredAt());

                // Prepare the query
                var enqueueUserStatement = database.getConnection().prepareStatement(
                        "INSERT INTO Queue (userId, position, joinedAt, channelId) VALUES (?, ?, ?, ?);"
                );

                // Set the query parameters
                enqueueUserStatement.setInt(1, user.getId());            // User ID
                enqueueUserStatement.setInt(2, getQueue(channel).size());   // Position
                enqueueUserStatement.setTimestamp(3, joinedAt);             // Joined At
                enqueueUserStatement.setInt(4, channel.getId());            // Channel ID

                // Execute the query
                enqueueUserStatement.executeUpdate();

                // Close resources
                enqueueUserStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to enqueue user", e);
            }
        }).start();
    }

    public boolean dequeueUser(TwitchUser user, TwitchUser channel) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var dequeueUserStatement = database.getConnection().prepareStatement(
                    "DELETE FROM Queue WHERE userId = ? AND channelId = ?;"
            );

            // Set the query parameters
            dequeueUserStatement.setInt(1, user.getId());       // User ID
            dequeueUserStatement.setInt(2, channel.getId());    // Channel ID

            // Execute the query
            var rowsAffected = dequeueUserStatement.executeUpdate();

            // Close resources
            dequeueUserStatement.close();

            // Return
            return rowsAffected == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to dequeue user", e);
        }
    }

    public void clearQueue(TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Prepare the query
                var clearQueueStatement = database.getConnection().prepareStatement(
                        "DELETE FROM Queue WHERE channelId = ?;"
                );

                // Set the query parameters
                clearQueueStatement.setInt(1, channel.getId()); // Channel ID

                // Execute the query
                clearQueueStatement.executeUpdate();

                // Close resources
                clearQueueStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to clear queue", e);
            }
        }).start();
    }

    public ArrayList<TwitchUser> getQueue(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getQueueStatement = database.getConnection().prepareStatement(
                    "SELECT user FROM Queue JOIN User ON userId = id WHERE channelId = ? ORDER BY position;"
            );

            // Set the query parameters
            getQueueStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            var resultSet = getQueueStatement.executeQuery();

            // Process the results
            ArrayList<TwitchUser> queue = new ArrayList<>();
            while (resultSet.next()) queue.add(inflateTwitchUser(resultSet.getBytes("user")));

            // Close resources
            resultSet.close();
            getQueueStatement.close();

            // Return
            return queue;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get queue", e);
        }
    }

    public Timestamp getJoinedAt(TwitchUser user, TwitchUser channel) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getEnqueueTimeStatement = database.getConnection().prepareStatement(
                    "SELECT joinedAt FROM Queue WHERE userId = ? AND channelId = ?;"
            );

            // Set the query parameters
            getEnqueueTimeStatement.setInt(1, user.getId());       // User ID
            getEnqueueTimeStatement.setInt(2, channel.getId());    // Channel ID

            // Execute the query
            var resultSet = getEnqueueTimeStatement.executeQuery();

            // Process the result
            Timestamp enqueueTime = null;
            if (resultSet.next()) enqueueTime = resultSet.getTimestamp("joinedAt");

            // Close resources
            resultSet.close();
            getEnqueueTimeStatement.close();

            // Return
            return enqueueTime;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get enqueue time", e);
        }
    }
}