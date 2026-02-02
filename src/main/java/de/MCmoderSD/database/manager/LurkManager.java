package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.helix.objects.TwitchUser;
import de.MCmoderSD.objects.MessageEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;

import static de.MCmoderSD.utilities.FormatUUID.asBytes;
import static de.MCmoderSD.utilities.ZipUtil.inflateEvent;
import static de.MCmoderSD.utilities.ZipUtil.inflateTwitchUser;

public class LurkManager {

    // Associations
    private final Database database;

    // Constructor
    public LurkManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;
    }

    public void addLurk(MessageEvent event) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent event cannot be null");

                // Ensure the event is logged
                database.getEventLogManager().waitTillMessageLogged(event.getId(), 10);

                // Insert lurk entry
                PreparedStatement insertLurkStatement = database.getConnection().prepareStatement(
                        "INSERT INTO Lurker (eventId, lurkerId) VALUES (?, ?);"
                );

                // Set the insert values
                insertLurkStatement.setBytes(1, asBytes(event.getId()));    // Event ID
                insertLurkStatement.setInt(2, event.getUser().getId());     // Lurker ID

                // Execute the statement
                insertLurkStatement.executeUpdate();

                // Close the statement
                insertLurkStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add lurk entry", e);
            }
        }).start();
    }

    public void addTraitor(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

                // Insert traitor entry
                PreparedStatement insertTraitorStatement = database.getConnection().prepareStatement(
                        "UPDATE Lurker SET traitor = ? WHERE lurkerId = ?;"
                );

                // Set the insert values
                insertTraitorStatement.setBoolean(1, true);     // Traitor Flag
                insertTraitorStatement.setInt(2, user.getId()); // Lurker ID

                // Execute the statement
                insertTraitorStatement.executeUpdate();

                // Close the statement
                insertTraitorStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to add traitor entry", e);
            }
        }).start();
    }

    public void removeLurk(TwitchUser user) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

                // Delete lurk entry
                PreparedStatement deleteLurkStatement = database.getConnection().prepareStatement(
                        "DELETE IGNORE FROM Lurker WHERE lurkerId = ?;"
                );

                // Set the delete values
                deleteLurkStatement.setInt(1, user.getId()); // Lurker ID

                // Execute the statement
                deleteLurkStatement.executeUpdate();

                // Close the statement
                deleteLurkStatement.close();

            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove lurk entry", e);
            }
        }).start();
    }

    public Timestamp getLurkTime(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

            // Query lurk time
            PreparedStatement queryLurkTimeStatement = database.getConnection().prepareStatement(
                    "SELECT e.firedAt FROM Lurker l, MessageEvent e WHERE lurkerId = ? AND e.id = l.eventId;"
            );

            // Set the query values
            queryLurkTimeStatement.setInt(1, user.getId()); // Lurker

            // Execute the query
            var resultSet = queryLurkTimeStatement.executeQuery();

            // Variable
            Timestamp startTime = null;

            // Process results
            if (resultSet.next()) startTime = resultSet.getTimestamp("firedAt");

            // Close resources
            resultSet.close();
            queryLurkTimeStatement.close();

            // Check if lurk time was found
            if (startTime == null) throw new RuntimeException("Lurk time not found for user ID: " + user.getId());

            // Return lurk time
            return startTime;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve lurk time", e);
        }
    }

    public MessageEvent getLurkEvent(TwitchUser user) {
        try {

            // Check Parameters
            if (user == null) throw new IllegalArgumentException("TwitchUser user cannot be null");

            // Query lurk event
            PreparedStatement queryLurkEventStatement = database.getConnection().prepareStatement(
                    "SELECT event FROM MessageEvent e, Lurker l WHERE lurkerId = ? AND e.id = l.eventId;"
            );

            // Set the query values
            queryLurkEventStatement.setInt(1, user.getId()); // Lurker

            // Execute the query
            var resultSet = queryLurkEventStatement.executeQuery();

            // Variable
            MessageEvent lurkEvent = null;

            // Process results
            if (resultSet.next()) lurkEvent = inflateEvent(resultSet.getBytes("event"));

            // Close resources
            resultSet.close();
            queryLurkEventStatement.close();

            // Check if lurk event was found
            if (lurkEvent == null) throw new RuntimeException("Lurk event not found for user ID: " + user.getId());

            // Return lurk event
            return lurkEvent;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve lurk event", e);
        }
    }

    public HashMap<TwitchUser, TwitchUser> getLurks() {
        try {

            // Query lurk entries
            PreparedStatement queryLurkStatement = database.getConnection().prepareStatement(
                    """
                    SELECT lurkerUser.user AS lurker, channelUser.user AS channel
                    FROM Lurker
                    JOIN MessageEvent ON eventId = id                       # Event Join
                    JOIN User AS lurkerUser ON lurkerId = lurkerUser.id     # Lurker Join
                    JOIN User AS channelUser ON channelId = channelUser.id  # Channel Join
                    """
            );

            // Execute the query
            var resultSet = queryLurkStatement.executeQuery();

            // Variables
            HashMap<TwitchUser, TwitchUser> lurkMap = new HashMap<>();

            // Process results
            while (resultSet.next()) lurkMap.put(
                    inflateTwitchUser(resultSet.getBytes("lurker")),    // Lurker
                    inflateTwitchUser(resultSet.getBytes("channel"))    // Channel
            );

            // Close resources
            resultSet.close();
            queryLurkStatement.close();

            // Return lurk map
            return lurkMap;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve lurk entries", e);
        }
    }

    public HashSet<TwitchUser> getTraitors() {
        try {

            // Query traitors entries
            PreparedStatement queryTraitorsStatement = database.getConnection().prepareStatement(
                    "SELECT user FROM Lurker JOIN User ON Lurker.lurkerId = User.id WHERE traitor = TRUE"
            );

            // Execute the query
            var resultSet = queryTraitorsStatement.executeQuery();

            // Variables
            HashSet<TwitchUser> traitorSet = new HashSet<>();

            // Process results
            while (resultSet.next()) traitorSet.add(inflateTwitchUser(resultSet.getBytes("user")));

            // Close resources
            resultSet.close();
            queryTraitorsStatement.close();

            // Return traitor set
            return traitorSet;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve traitor entries", e);
        }
    }
}