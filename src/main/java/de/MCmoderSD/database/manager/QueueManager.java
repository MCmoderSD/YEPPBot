package de.MCmoderSD.database.manager;

import de.MCmoderSD.commands.Queue;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.SQLException;
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

    // Helper Method
    private void ensureQueue(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var ensureQueueStatement = database.getConnection().prepareStatement(
                    "INSERT IGNORE INTO Queue (id) VALUES (?);"
            );

            // Set the query parameter
            ensureQueueStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            ensureQueueStatement.executeUpdate();

            // Close resources
            ensureQueueStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure queue entry: " + e.getMessage(), e);
        }
    }

    // Set the Queue State
    public boolean setOpen(TwitchUser channel, boolean open) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Ensure Queue Entry
            ensureQueue(channel);

            // Prepare the query
            var setOpenStatement = database.getConnection().prepareStatement(
                    open
                            ? "UPDATE Queue SET isOpen = b'1' WHERE id = ? AND isOpen = b'0';"
                            : "UPDATE Queue SET isOpen = b'0' WHERE id = ? AND isOpen = b'1';"
            );

            // Set the query parameter
            setOpenStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            var affectedRows = setOpenStatement.executeUpdate();

            // Close resources
            setOpenStatement.close();

            // Return whether the state actually changed
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to set the queue state: " + e.getMessage(), e);
        }
    }

    // Get the Queue State
    public boolean isOpen(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var isOpenStatement = database.getConnection().prepareStatement(
                    "SELECT isOpen FROM Queue WHERE id = ?;"
            );

            // Set the query parameter
            isOpenStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            var resultSet = isOpenStatement.executeQuery();

            // Process the result
            var isOpen = resultSet.next() && resultSet.getBoolean("isOpen");

            // Close resources
            resultSet.close();
            isOpenStatement.close();

            // Return, a channel without an entry counts as closed
            return isOpen;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve the queue state: " + e.getMessage(), e);
        }
    }

    // Get the Queue Requirement
    public Queue.Requirement getRequirement(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getRequirementStatement = database.getConnection().prepareStatement(
                    "SELECT requirement FROM Queue WHERE id = ?;"
            );

            // Set the query parameter
            getRequirementStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            var resultSet = getRequirementStatement.executeQuery();

            // Process the result
            var requirement = Queue.Requirement.EVERYONE;
            if (resultSet.next()) requirement = Queue.Requirement.fromString(resultSet.getString("requirement"));

            // Close resources
            resultSet.close();
            getRequirementStatement.close();

            // Return the requirement
            return requirement;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve the queue requirement: " + e.getMessage(), e);
        }
    }

    // Get the Position of a User in the Queue
    public int getPosition(TwitchUser user, TwitchUser channel) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getPositionStatement = database.getConnection().prepareStatement(
                    "SELECT FIND_IN_SET(?, queue) AS position FROM Queue WHERE id = ?;"
            );

            // Set the query parameters
            getPositionStatement.setInt(1, user.getId());       // User ID
            getPositionStatement.setInt(2, channel.getId());    // Channel ID

            // Execute the query
            var resultSet = getPositionStatement.executeQuery();

            // Process the result
            var position = 0;
            if (resultSet.next()) position = resultSet.getInt("position");

            // Close resources
            resultSet.close();
            getPositionStatement.close();

            // Return the position, a channel without an entry counts as not queued
            return position;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get position: " + e.getMessage(), e);
        }
    }

    // Enqueue User
    public boolean enqueueUser(TwitchUser user, TwitchUser channel) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Ensure Queue Entry
            ensureQueue(channel);

            // Prepare the query
            var enqueueUserStatement = database.getConnection().prepareStatement(
                    "UPDATE Queue SET queue = CONCAT(queue, IF(queue = '', '', ','), ?) WHERE id = ? AND FIND_IN_SET(?, queue) = 0 AND isOpen = b'1' AND (LENGTH(queue) - LENGTH(REPLACE(queue, ',', '')) + 1) < 100;"
            );

            // Set the query parameters
            enqueueUserStatement.setInt(1, user.getId());       // User ID
            enqueueUserStatement.setInt(2, channel.getId());    // Channel ID
            enqueueUserStatement.setInt(3, user.getId());       // User ID

            // Execute the query
            var affectedRows = enqueueUserStatement.executeUpdate();

            // Close resources
            enqueueUserStatement.close();

            // Return whether the user was enqueued
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to enqueue user: " + e.getMessage(), e);
        }
    }

    // Dequeue User
    public boolean dequeueUser(TwitchUser user, TwitchUser channel) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Ensure Queue Entry
            ensureQueue(channel);

            // Prepare the query
            var dequeueUserStatement = database.getConnection().prepareStatement(
                    "UPDATE Queue SET queue = TRIM(BOTH ',' FROM REPLACE(CONCAT(',', queue, ','), CONCAT(',', ?, ','), ',')) WHERE id = ? AND FIND_IN_SET(?, queue) > 0;"
            );

            // Set the query parameters
            dequeueUserStatement.setInt(1, user.getId());        // User ID
            dequeueUserStatement.setInt(2, channel.getId());     // Channel ID
            dequeueUserStatement.setInt(3, user.getId());        // User ID

            // Execute the query
            var affectedRows = dequeueUserStatement.executeUpdate();

            // Close resources
            dequeueUserStatement.close();

            // Return whether the user was dequeued
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to dequeue user: " + e.getMessage(), e);
        }
    }

    // Clear Queue
    public void clearQueue(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Ensure Queue Entry
            ensureQueue(channel);

            // Prepare the query
            var clearQueueStatement = database.getConnection().prepareStatement(
                    "UPDATE Queue SET queue = '' WHERE id = ?;"
            );

            // Set the query parameter
            clearQueueStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            clearQueueStatement.executeUpdate();

            // Close resources
            clearQueueStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear queue: " + e.getMessage(), e);
        }
    }

    // Get the Queue
    public ArrayList<TwitchUser> getQueue(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

            // Prepare the query
            var getQueueStatement = database.getConnection().prepareStatement(
                    "SELECT u.user FROM Queue q JOIN User u ON FIND_IN_SET(u.id, q.queue) > 0 WHERE q.id = ? ORDER BY FIND_IN_SET(u.id, q.queue);"
            );

            // Set the query parameter
            getQueueStatement.setInt(1, channel.getId()); // Channel ID

            // Execute the query
            var resultSet = getQueueStatement.executeQuery();

            // Process the results
            var queue = new ArrayList<TwitchUser>();
            while (resultSet.next()) queue.add(inflateTwitchUser(resultSet.getBytes("user")));

            // Close resources
            resultSet.close();
            getQueueStatement.close();

            // Return the queue
            return queue;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get queue: " + e.getMessage(), e);
        }
    }
}