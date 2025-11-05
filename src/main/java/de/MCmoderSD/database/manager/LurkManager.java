package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;

public class LurkManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public LurkManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }

    public void addLurk(TwitchUser user, TwitchUser channel) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");
                if (channel == null) throw new IllegalArgumentException("TwitchUser channel cannot be null");

                // Insert lurk entry
                PreparedStatement insertLurkStatement = connection.prepareStatement(
                        "INSERT INTO Lurker (lurkerId, channelId, timestamp) VALUES (?, ?, ?);"
                );

                // Set the insert values
                insertLurkStatement.setInt(1, user.getId());                                    // Lurker
                insertLurkStatement.setInt(2, channel.getId());                                 // Channel
                insertLurkStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Timestamp

                // Execute the statement
                insertLurkStatement.executeUpdate();

                // Close the statement
                insertLurkStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add lurk entry", e);
            }
        }).start();
    }

    public void removeLurk(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

                // Delete lurk entry
                PreparedStatement deleteLurkStatement = connection.prepareStatement(
                        "DELETE FROM Lurker WHERE lurkerId = ?;"
                );

                // Set the delete values
                deleteLurkStatement.setInt(1, user.getId()); // Lurker

                // Execute the statement
                deleteLurkStatement.executeUpdate();

                // Close the statement
                deleteLurkStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove lurk entry", e);
            }
        }).start();
    }

    public void addTraitor(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

                // Insert traitor entry
                PreparedStatement insertTraitorStatement = connection.prepareStatement(
                        "UPDATE Lurker SET traitor = ? WHERE lurkerId = ?;"
                );

                // Set the insert values
                insertTraitorStatement.setBoolean(1, true);
                insertTraitorStatement.setInt(2, user.getId()); // Lurker

                // Execute the statement
                insertTraitorStatement.executeUpdate();

                // Close the statement
                insertTraitorStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add traitor entry", e);
            }
        }).start();
    }

    public Timestamp getLurkTime(TwitchUser user) {
        try {

            // Query lurk time
            PreparedStatement queryLurkTimeStatement = connection.prepareStatement(
                    "SELECT timestamp FROM Lurker WHERE lurkerId = ?;"
            );

            // Set the query values
            queryLurkTimeStatement.setInt(1, user.getId()); // Lurker

            // Execute the query
            var resultSet = queryLurkTimeStatement.executeQuery();

            // Variable
            Timestamp startTime = null;

            // Process results
            if (resultSet.next()) startTime = resultSet.getTimestamp("timestamp");

            // Close resources
            resultSet.close();
            queryLurkTimeStatement.close();

            // Check result
            if (startTime == null) throw new IllegalStateException("No lurk entry found for user with ID " + user.getId());

            // Return lurk time
            return startTime;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve lurk time", e);
        }
    }

    public HashMap<Integer, Integer> getLurks() {
        try {

            // Query lurk entries
            PreparedStatement queryLurkStatement = connection.prepareStatement(
                    "SELECT lurkerId, channelId FROM Lurker;"
            );

            // Execute the query
            var resultSet = queryLurkStatement.executeQuery();

            // Variables
            HashMap<Integer, Integer> lurkMap = new HashMap<>();

            // Process results
            while (resultSet.next()) lurkMap.put(resultSet.getInt("lurkerId"), resultSet.getInt("channelId"));

            // Close resources
            resultSet.close();
            queryLurkStatement.close();

            // Return lurk map
            return lurkMap;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve lurk entries", e);
        }
    }

    public HashSet<Integer> getTraitor() {
        try {

            // Query traitor entries
            PreparedStatement queryTraitorStatement = connection.prepareStatement(
                    "SELECT lurkerId FROM Lurker WHERE traitor = TRUE;"
            );

            // Execute the query
            var resultSet = queryTraitorStatement.executeQuery();

            // Variables
            HashSet<Integer> traitorSet = new HashSet<>();

            // Process results
            while (resultSet.next()) traitorSet.add(resultSet.getInt("lurkerId"));

            // Close resources
            resultSet.close();
            queryTraitorStatement.close();

            // Return traitor set
            return traitorSet;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve traitor entries", e);
        }
    }
}
