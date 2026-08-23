package de.MCmoderSD.database.manager;

import de.MCmoderSD.data.TimerState;
import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.SQLException;

public class SubathonManager {

    // Associations
    private final Database database;

    // Constructor
    public SubathonManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    // Ensure the Timer Entry of a Channel exists
    public void ensureTimer(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Prepare the query
            var ensureTimerStatement = database.getConnection().prepareStatement(
                    "INSERT IGNORE INTO SubathonTimer (id) VALUES (?);"
            );

            // Set the query parameter
            ensureTimerStatement.setInt(1, channel.getId());    // Channel ID

            // Execute the query
            ensureTimerStatement.executeUpdate();

            // Close resources
            ensureTimerStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure timer entry: " + e.getMessage(), e);
        }
    }

    // Start the Timer, converts the remaining seconds into an end timestamp
    public boolean startTimer(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the query
            var startTimerStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET running = TRUE, endsAt = DATE_ADD(UTC_TIMESTAMP(3), INTERVAL remaining SECOND), remaining = 0 WHERE id = ? AND running = FALSE;"
            );

            // Set the query parameter
            startTimerStatement.setInt(1, channel.getId());     // Channel ID

            // Execute the query
            var affectedRows = startTimerStatement.executeUpdate();

            // Close resources
            startTimerStatement.close();

            // Return whether the Timer was started
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to start the timer: " + e.getMessage(), e);
        }
    }

    // Pause the Timer, converts the end timestamp back into remaining seconds
    public boolean pauseTimer(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the query
            var pauseTimerStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET running = FALSE, remaining = GREATEST(0, TIMESTAMPDIFF(SECOND, UTC_TIMESTAMP(3), endsAt)), endsAt = NULL WHERE id = ? AND running = TRUE;"
            );

            // Set the query parameter
            pauseTimerStatement.setInt(1, channel.getId());     // Channel ID

            // Execute the query
            var affectedRows = pauseTimerStatement.executeUpdate();

            // Close resources
            pauseTimerStatement.close();

            // Return whether the Timer was paused
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to pause the timer: " + e.getMessage(), e);
        }
    }

    // Add Time, both statements are sent, the running flag decides which one takes effect
    public void addTime(TwitchUser channel, int seconds) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (seconds < 0) throw new IllegalArgumentException("Seconds cannot be negative");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the running query, GREATEST revives a timer that already ran out
            var addRunningStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET endsAt = DATE_ADD(GREATEST(endsAt, UTC_TIMESTAMP(3)), INTERVAL ? SECOND) WHERE id = ? AND running = TRUE;"
            );

            // Set the query parameters
            addRunningStatement.setInt(1, seconds);             // Seconds to add
            addRunningStatement.setInt(2, channel.getId());     // Channel ID

            // Execute the query
            addRunningStatement.executeUpdate();

            // Close resources
            addRunningStatement.close();

            // Prepare the paused query
            var addPausedStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET remaining = remaining + ? WHERE id = ? AND running = FALSE;"
            );

            // Set the query parameters
            addPausedStatement.setInt(1, seconds);              // Seconds to add
            addPausedStatement.setInt(2, channel.getId());      // Channel ID

            // Execute the query
            addPausedStatement.executeUpdate();

            // Close resources
            addPausedStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add time: " + e.getMessage(), e);
        }
    }

    // Remove Time, clamped at zero so the timer never runs into the negative
    public void removeTime(TwitchUser channel, int seconds) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (seconds < 0) throw new IllegalArgumentException("Seconds cannot be negative");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the running query, GREATEST clamps the timer at zero
            var removeRunningStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET endsAt = GREATEST(DATE_SUB(endsAt, INTERVAL ? SECOND), UTC_TIMESTAMP(3)) WHERE id = ? AND running = TRUE;"
            );

            // Set the query parameters
            removeRunningStatement.setInt(1, seconds);          // Seconds to remove
            removeRunningStatement.setInt(2, channel.getId());  // Channel ID

            // Execute the query
            removeRunningStatement.executeUpdate();

            // Close resources
            removeRunningStatement.close();

            // Prepare the paused query
            var removePausedStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET remaining = GREATEST(0, remaining - ?) WHERE id = ? AND running = FALSE;"
            );

            // Set the query parameters
            removePausedStatement.setInt(1, seconds);           // Seconds to remove
            removePausedStatement.setInt(2, channel.getId());   // Channel ID

            // Execute the query
            removePausedStatement.executeUpdate();

            // Close resources
            removePausedStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove time: " + e.getMessage(), e);
        }
    }

    // Set Time keeps the running state and only replaces the value
    public void setTime(TwitchUser channel, int seconds) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");
            if (seconds < 0) throw new IllegalArgumentException("Seconds cannot be negative");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the query
            var setTimeStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET endsAt = IF(running, DATE_ADD(UTC_TIMESTAMP(3), INTERVAL ? SECOND), NULL), remaining = IF(running, 0, ?) WHERE id = ?;"
            );

            // Set the query parameters
            setTimeStatement.setInt(1, seconds);                // Seconds while running
            setTimeStatement.setInt(2, seconds);                // Seconds while paused
            setTimeStatement.setInt(3, channel.getId());        // Channel ID

            // Execute the query
            setTimeStatement.executeUpdate();

            // Close resources
            setTimeStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to set time: " + e.getMessage(), e);
        }
    }

    // Reset the Timer to the start value configured in the dashboard
    public void resetTimer(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Ensure Timer Entry
            ensureTimer(channel);

            // Prepare the query
            var resetTimerStatement = database.getConnection().prepareStatement(
                    "UPDATE SubathonTimer SET running = FALSE, endsAt = NULL, remaining = startSeconds WHERE id = ?;"
            );

            // Set the query parameter
            resetTimerStatement.setInt(1, channel.getId());     // Channel ID

            // Execute the query
            resetTimerStatement.executeUpdate();

            // Close resources
            resetTimerStatement.close();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset the timer: " + e.getMessage(), e);
        }
    }

    // Get the current Timer State, the database calculates the remaining seconds
    public TimerState getTimer(TwitchUser channel) {
        try {

            // Check Parameters
            if (channel == null) throw new IllegalArgumentException("Channel cannot be null");

            // Prepare the query
            var getTimerStatement = database.getConnection().prepareStatement(
                    "SELECT running, IF(running, GREATEST(0, TIMESTAMPDIFF(SECOND, UTC_TIMESTAMP(3), endsAt)), remaining) AS remaining FROM SubathonTimer WHERE id = ?;"
            );

            // Set the query parameter
            getTimerStatement.setInt(1, channel.getId());       // Channel ID

            // Execute the query
            var resultSet = getTimerStatement.executeQuery();

            // Variable
            TimerState timerState = null;

            // Process the result
            if (resultSet.next()) {

                // Retrieve data
                var running = resultSet.getBoolean("running");
                var remaining = resultSet.getInt("remaining");

                // Create Timer State
                timerState = new TimerState(running, remaining);
            }

            // Close resources
            resultSet.close();
            getTimerStatement.close();

            // Return an empty state if the channel has no entry yet
            return timerState == null ? new TimerState(false, 0) : timerState;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve timer: " + e.getMessage(), e);
        }
    }
}