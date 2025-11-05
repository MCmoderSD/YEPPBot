package de.MCmoderSD.database.manager;

import de.MCmoderSD.database.Database;
import de.MCmoderSD.objects.FollowEvent;
import de.MCmoderSD.objects.MessageEvent;
import de.MCmoderSD.objects.RaidEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static de.MCmoderSD.tools.GZIP.deflateObject;
import static de.MCmoderSD.utilities.FormatUUID.asBytes;
import static de.MCmoderSD.utilities.Hasher.xxHash64;

public class EventLogManager {

    // Associations
    private final Database database;

    // Attributes
    private final Connection connection;

    // Constructor
    public EventLogManager(Database database) {

        // Check Parameters
        if (database == null) throw new IllegalArgumentException("Database cannot be null");

        // Set Associations
        this.database = database;

        // Set Attributes
        connection = database.getConnection();
    }

    public void logMessageEvent(MessageEvent event) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("MessageEvent cannot be null");

                // Variables
                byte[] contentHash = xxHash64(event.getMessage());

                // Insert message content
                PreparedStatement insertContentStatement = connection.prepareStatement(
                        "INSERT IGNORE INTO MessageContent (hash, content) VALUES (?, ?);"
                );

                // Set the insert values
                insertContentStatement.setBytes(1, contentHash);            // Content hash
                insertContentStatement.setString(2, event.getMessage());    // Message content

                // Execute the statement
                insertContentStatement.executeUpdate();

                // Close the statement
                insertContentStatement.close();

                // Insert message event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO MessageEvent (id, firedAt, channelId, userId, content, deviceType, subTier, subMonths, action, highlighted, firstMessage, userIntroduction, skipSubsModeMessage, event) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(event.getId()));                   // Event ID
                insertEventStatement.setTimestamp(2, Timestamp.from(event.getFiredAt()));   // Event fired at timestamp
                insertEventStatement.setInt(3, event.getChannel().getId());                 // Channel ID
                insertEventStatement.setInt(4, event.getUser().getId());                    // User ID
                insertEventStatement.setBytes(5, contentHash);                              // Message content Hash
                insertEventStatement.setString(6, event.getDeviceType().name());            // Device Type
                insertEventStatement.setString(7, event.getSubTier().name());               // Subscription Tier
                insertEventStatement.setInt(8, event.getSubMonths());                       // Subscription Months
                insertEventStatement.setBoolean(9, event.isAction());                       // Action Message (/me)
                insertEventStatement.setBoolean(10, event.isHighlighted());                 // Highlighted Message
                insertEventStatement.setBoolean(11, event.isFirstMessage());                // First Message
                insertEventStatement.setBoolean(12, event.isUserIntroduction());            // User Introduction
                insertEventStatement.setBoolean(13, event.isSkipSubsModeMessage());         // Skip Subs Mode Message
                insertEventStatement.setBytes(14, deflateObject(event));                    // Full Event Data (compressed)

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

                connection.commit();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to log MessageEvent: " + e.getMessage(), e);
            }
        }).start();
    }

    public void logRaidEvent(RaidEvent event) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("RaidEvent cannot be null");

                // Insert raid event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO RaidEvent (id, firedAt, channelId, userId, viewers, event) VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setBytes(1, asBytes(event.getId()));                   // Event ID
                insertEventStatement.setTimestamp(2, Timestamp.from(event.getFiredAt()));   // Event fired at timestamp
                insertEventStatement.setInt(3, event.getChannel().getId());                 // Channel ID   (target)
                insertEventStatement.setInt(4, event.getUser().getId());                    // Raider ID    (source)
                insertEventStatement.setInt(5, event.getViewers());                         // Viewers
                insertEventStatement.setBytes(6, deflateObject(event));                     // Full Event Data (compressed)

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to log RaidEvent: " + e.getMessage(), e);
            }
        }).start();
    }

    public void logFollowEvent(FollowEvent event) {
        new Thread(() -> {
            try {

                // Check Parameters
                if (event == null) throw new IllegalArgumentException("FollowEvent cannot be null");

                // Variables
                byte[] eventData = deflateObject(event);

                // Insert raid event
                PreparedStatement insertEventStatement = connection.prepareStatement(
                        "INSERT INTO FollowEvent (followedAt, channelId, userId, event) VALUES (?, ?, ?, ?);"
                );

                // Set the insert values
                insertEventStatement.setTimestamp(1, Timestamp.from(event.getFollowedAt()));   // Followed At Timestamp
                insertEventStatement.setInt(2, event.getChannel().getId());                    // Channel
                insertEventStatement.setInt(3, event.getUser().getId());                       // User
                insertEventStatement.setBytes(4, eventData);                                   // Full Event Data (compressed)

                // Execute the statement
                insertEventStatement.executeUpdate();

                // Close the statement
                insertEventStatement.close();

            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to log RaidEvent: " + e.getMessage(), e);
            }
        }).start();
    }
}
